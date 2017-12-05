package com.ievolutioned.auria.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.ievolutioned.auria.MainActivity;
import com.ievolutioned.auria.R;
import com.ievolutioned.auria.entity.ProfileEntity;
import com.ievolutioned.auria.net.CloudImageTask;
import com.ievolutioned.auria.net.service.ProfileService;
import com.ievolutioned.auria.net.service.ResponseBase;
import com.ievolutioned.auria.util.AppConfig;
import com.ievolutioned.auria.util.AppPreferences;
import com.ievolutioned.auria.util.ImageFilePath;
import com.ievolutioned.auria.util.LogUtil;
import com.ievolutioned.auria.view.ViewUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MyProfileFragment class, represents the portion of UI for my profile and password controls
 */
public class MyProfileFragment extends Fragment {
    /**
     * TAG
     */
    public final static String TAG = MyProfileFragment.class.getName();
    /**
     * Main ViewPager pager
     */
    private ViewPager mViewPager;
    /**
     * PagerTabStrip tab strip
     */
    private PagerTabStrip mPagerTabStrip;
    /**
     * A set of Fragments
     */
    private List<Fragment> mFragments = new ArrayList<>(2);

    /**
     * ProfileFragment profile fragment
     */
    protected ProfileFragment profileFragment;
    /**
     * PasswordFragment password fragment
     */
    protected PasswordFragment passwordFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View root = inflater.inflate(R.layout.fragment_myprofile, container, false);
        setHasOptionsMenu(true);
        bindUI(root);
        bindData(getArguments());
        setTitle(getString(R.string.string_fragment_myprofile_title));
        return root;
    }

    private void setTitle(String title) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof MainActivity)
            ((MainActivity) activity).setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.fragment_profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_fragment_profile_upload)
            if (validateForm())
                uploadProfile();
        return true;
    }

    /**
     * Binds the User interface
     */
    private void bindUI(View root) {
        mViewPager = (ViewPager) root.findViewById(R.id.fragment_profile_pager);
        mPagerTabStrip = (PagerTabStrip) root.findViewById(R.id.fragment_profile_pager_tab_strip);

        if (mViewPager == null) {
            //TODO: when it is a tablet
        }
    }

    /**
     * Binds the data to the current fragments
     *
     * @param arguments - Bundle of arguments
     */
    private void bindData(Bundle arguments) {
        if (mViewPager != null) {
            profileFragment = new ProfileFragment();
            passwordFragment = new PasswordFragment();
            mFragments.add(0, profileFragment);
            mFragments.add(1, passwordFragment);
            ProfilePageAdapter adapter = new ProfilePageAdapter(getChildFragmentManager());
            mViewPager.setAdapter(adapter);
        } else {
            profileFragment = (ProfileFragment) getChildFragmentManager().findFragmentByTag("Profile");
            passwordFragment = (PasswordFragment) getChildFragmentManager().findFragmentByTag("Password");
        }
        loadMyProfileInfo();
    }

    /**
     * Loads the profile information
     */
    private void loadMyProfileInfo() {
        final AlertDialog loading = ViewUtility.getLoadingScreen(getActivity());
        loading.show();
        new ProfileService(AppConfig.getUUID(getActivity()), AppPreferences.getAdminToken(getActivity())).getProfileInfo(
                new ProfileService.ProfileServiceHandler() {
                    @Override
                    public void onSuccess(ProfileService.ProfileResponse response) {
                        LogUtil.d(TAG, response.msg);
                        loading.dismiss();
                        setMyProfileInfo(response.profile);
                    }

                    @Override
                    public void onError(ProfileService.ProfileResponse response) {
                        LogUtil.e(TAG, response.msg, response.e);
                        loading.dismiss();
                        ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                                R.string.string_fragment_myprofile_error_load);
                    }

                    @Override
                    public void onCancel() {
                        loading.dismiss();
                    }
                }
        );
    }

    /**
     * Set the info for profile fragment
     *
     * @param profile - ProfileEntity profile
     */
    private void setMyProfileInfo(ProfileEntity profile) {
        if (profileFragment == null)
            return;
        profileFragment.setProfileInfo(profile);
    }

    /**
     * Validates the form
     *
     * @return true if it valid, false otherwise
     */
    private boolean validateForm() {
        boolean p = false;
        boolean c = false;
        if (profileFragment != null)
            p = !(profileFragment.getEmail() == null && profileFragment.getImagePath() == null);
        if (passwordFragment != null)
            c = !(passwordFragment.getPassword() == null && passwordFragment.getRepassword() == null);
        if (c == false && p == false) {
            ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                    R.string.string_fragment_myprofile_no_changes);
            return false;
        }
        if (c)
            if (passwordFragment.getPassword() == null ||
                    passwordFragment.getRepassword() == null ||
                    !passwordFragment.getPassword().contentEquals(passwordFragment.getRepassword())) {
                focusPasswordFragment();
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                        R.string.string_fragment_myprofile_error_password);
                return false;
            }
        return true;
    }

    /**
     * Focuses the password fragment if it is a view pager
     */
    private void focusPasswordFragment() {
        if (mViewPager != null)
            mViewPager.setCurrentItem(1, true);
    }

    /**
     * Get prepared for upload profile info
     */
    private void uploadProfile() {
        String picturePath = profileFragment.getImagePath();
        //Verify picture edit
        if (picturePath != null) {
            uploadProfilePicture(picturePath);

        } else
            uploadProfile(null);

    }

    /**
     * Upload the data of the profile
     *
     * @param picture
     */
    private void uploadProfile(final String picture) {
        //Get info
        final String email = profileFragment.getEmail();
        final String password = passwordFragment.getPassword();
        final String repassword = passwordFragment.getRepassword();

        JsonObject response = new JsonObject();
        if (email != null)
            response.addProperty("email", email);
        if (password != null && repassword != null) {
            response.addProperty("password", password);
            response.addProperty("password_confirmation", repassword);
        }
        if (picture != null)
            response.addProperty("avatar_cloudinary", picture);

        JsonObject info = new JsonObject();
        info.add("admin", response);
        ProfileService profileService = new ProfileService(AppConfig.getUUID(getActivity()),
                AppPreferences.getAdminToken(getActivity()));
        final AlertDialog loading = ViewUtility.getLoadingScreen(getActivity());
        loading.show();

        profileService.updateInfo(info.getAsJsonObject().toString(), new ProfileService.ProfileServiceHandler() {
            @Override
            public void onSuccess(ProfileService.ProfileResponse response) {
                profileFragment.setDefaultEmail(email);
                loading.dismiss();
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_SUCCESS,
                        R.string.string_fragment_myprofile_success);
                LogUtil.d(TAG, response.msg);
            }

            @Override
            public void onError(ProfileService.ProfileResponse response) {
                loading.dismiss();
                LogUtil.e(TAG, response.msg, response.e);
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                        R.string.string_fragment_myprofile_error);
            }

            @Override
            public void onCancel() {
                loading.dismiss();
                LogUtil.d(TAG, "Cancelado");
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                        R.string.string_fragment_myprofile_cancel);
            }
        });
    }

    /**
     * Uploads a profile picture
     *
     * @param path - Picture path
     */
    private void uploadProfilePicture(final String path) {
        final AlertDialog loading = ViewUtility.getLoadingScreen(getActivity());
        loading.show();
        CloudImageTask cloudImageTask = new CloudImageTask();
        File file = new File(path);
        cloudImageTask.uploadImageFile(file, new CloudImageTask.CloudImageHandler() {
            @Override
            public void onSuccess(ResponseBase response) {
                String urlCloudinary = ((CloudImageTask.UploadImageResponse) response).file;
                if (urlCloudinary != null) {
                    LogUtil.d(TAG, "URL:" + urlCloudinary);
                    loading.dismiss();
                    uploadProfile(urlCloudinary);
                }
            }

            @Override
            public void onError(ResponseBase response) {
                LogUtil.e(TAG, response.e.getMessage(), response.e);
                loading.dismiss();
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                        R.string.string_fragment_myprofile_error_picture);
            }

            @Override
            public void onCancel() {
                loading.dismiss();
                ViewUtility.showMessage(getActivity(), ViewUtility.MSG_ERROR,
                        R.string.string_fragment_myprofile_cancel);
            }
        });
    }

    /**
     * Sets the image according to the request code and Intent data
     *
     * @param data
     * @param requestCode
     */
    public void setImageByIntent(Intent data, int requestCode) {
        try {
            Bitmap bitmap = null;
            String path = null;
            if (requestCode == MainActivity.ACTION_PICK_PHOTO) {
                //Must get input stream for image
                InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                path = ImageFilePath.getPath(getActivity(), data.getData());
            } else if (requestCode == MainActivity.ACTION_TAKE_PHOTO) {
                //Must try for thumbnails
                bitmap = (Bitmap) data.getExtras().get("data");
                // get the temporal uri
                Uri tempUri = getImageUri(bitmap);
                // Gets the real path
                path = getRealPathFromURI(tempUri);
            }

            if (bitmap != null)
                profileFragment.setProfilePicture(bitmap);
            if (path != null)
                profileFragment.setImagePath(path);
        } catch (Exception ee) {
            LogUtil.e(TAG, ee.getMessage(), ee);
        }
    }

    /**
     * Gets the image Uri
     *
     * @param image
     * @return
     */
    public Uri getImageUri(Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), image,
                "ProfileTemp", null);
        return Uri.parse(path);
    }

    /**
     * Gets the real path
     *
     * @param uri - Uri uri
     * @return the real path of a uri file
     */
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    /**
     * ProfilePageAdapter class that allows the control of the FragmentPagerAdapter adapter of
     * page viewer
     */
    public class ProfilePageAdapter extends FragmentPagerAdapter {

        public ProfilePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position) instanceof ProfileFragment ?
                    getString(R.string.string_fragment_myprofile_profile) :
                    getString(R.string.string_fragment_myprofile_password);
        }
    }

}
