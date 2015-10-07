package com.ievolutioned.pxform;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Map;

/**
 *
 */
public class PXFSubMenuButton extends PXWidget {
    private String current_title = "";
    private int current_option = -1;
    private CharSequence[] options_array;
    private FragmentManager fragmentManager;
    private boolean isDialogShown = false;

    public static class HelperSubMenuButton extends HelperWidget {
        protected Button button;
        protected TextView title;
        protected LinearLayout linearButton;
    }

    public PXFSubMenuButton(Map<String, Map.Entry<String, JsonElement>> entry) {
        super(entry);
    }

    @Override
    protected HelperWidget generateHelperClass() {
        return new HelperSubMenuButton();
    }

    @Override
    public int getAdapterItemType() {
        return PXWidget.ADAPTER_ITEM_TYPE_SUBMENUBUTTON;
    }

    @Override
    public void setValue(String value) {
        try {
            current_title = value;
            if (options_array != null)
                for (int i = 0; i < options_array.length; i++)
                    if (options_array[i].toString().contentEquals(value)) {
                        current_option = i;
                    }
        } catch (Exception ignored) {
            current_option = -1;
        }
    }

    @Override
    public String getValue() {
        return current_title;
    }

    @Override
    public void setWidgetData(View v) {
        super.setWidgetData(v);
        HelperSubMenuButton helper = (HelperSubMenuButton) v.getTag();

        helper.title.setText(getJsonEntries().containsKey(FIELD_TITLE) ?
                getJsonEntries().get(FIELD_TITLE).getValue().getAsString() : " ");

        if (current_title != null && !current_title.isEmpty()) {
            helper.button.setText(current_title);
        } else {
            current_title = getJsonEntries().containsKey(FIELD_PLACEHOLDER) ?
                    getJsonEntries().get(FIELD_PLACEHOLDER).getValue().getAsString() : "";
            helper.button.setText(current_title);
        }


        /*
        if(current_option > -1){
            if(options_array != null && options_array.length > 0){
                helper.button.setText(options_array[current_option]);
            }
        }else {
            helper.button.setText(getJsonEntries().containsKey(FIELD_PLACEHOLDER) ?
                    getJsonEntries().get(FIELD_PLACEHOLDER).getValue().getAsString() : PLACEHOLDER_DEFAULT);
        }
        */


        helper.button.setOnClickListener(onclick);
    }

    @Override
    public View createControl(Activity context) {
        fragmentManager = context.getFragmentManager();
        setupOptionsArray();

        LinearLayout v = (LinearLayout) super.createControl(context);
        HelperSubMenuButton helper = (HelperSubMenuButton) v.getTag();

        //layout container
        LinearLayout linear = getGenericLinearLayout(context);
        linear.setWeightSum(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linear.setLayoutParams(params);
        linear.setOrientation(LinearLayout.HORIZONTAL);
        helper.linearButton = linear;

        //field name
        TextView text = getGenericTextView(context, getJsonEntries().containsKey(FIELD_TITLE) ?
                getJsonEntries().get(FIELD_TITLE).getValue().getAsString() : " ");
        params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        text.setLayoutParams(params);
        try {
            text.setTextSize(getDimen(context.getBaseContext(), R.dimen.default_text_size));
        } catch (Exception e) {
        }
        helper.title = text;

        //sub button menu
        Button button = new Button(context);
        params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        button.setLayoutParams(params);
        button.setText(getJsonEntries().containsKey(FIELD_PLACEHOLDER) ?
                getJsonEntries().get(FIELD_PLACEHOLDER).getValue().getAsString() : "");
        button.setOnClickListener(onclick);
        helper.button = button;

        //add controls to linear parent before main container
        linear.addView(text);
        linear.addView(button);

        //add controls to main container
        v.addView(linear);

        //add validation view
        View vv = getGenericValidationView(context);
        helper.validationView = vv;
        v.addView(vv);

        return v;
    }

    private void setupOptionsArray() {
        //options_array
        JsonElement cell = getJsonEntries().get(FIELD_OPTIONS).getValue();
        JsonArray array;
        array = cell.getAsJsonArray();
        options_array = new CharSequence[array.size()];

        for (int x = 0; x < array.size(); ++x) {
            cell = array.get(x);

            options_array[x] = cell.getAsJsonObject().entrySet().iterator().next().getKey();
        }
    }

    private View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDialogShown)
                return;

            ChildFormDialogFragment dialog = ChildFormDialogFragment.getInstance(PXFSubMenuButton.this);
            dialog.setISelectedIndex(new ChildFormDialogFragment.IIndexSelected() {
                @Override
                public void onIndexSelected(int sel) {
                    current_option = sel;
                    current_title = options_array[sel].toString();
                    //options_array
                    JsonElement cell = getJsonEntries().get(FIELD_OPTIONS).getValue();
                    JsonArray array = cell.getAsJsonArray();
                    cell = array.get(sel);
                    isDialogShown = false;

                    if (getEventHandler() != null) {
                        getEventHandler().notifyDataSetChanges();
                    }

                    if (!cell.getAsJsonObject().entrySet().iterator().next().getValue().isJsonArray() ||
                            cell.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonArray().size() < 1)
                        return;

                    //String option = cell.getAsJsonObject().entrySet().iterator().next().getKey();
                    String json = cell.getAsJsonObject().entrySet().iterator().next().getValue().toString();

                    if (getEventHandler() != null) {
                        getEventHandler().selectedSubForm(json, PXFSubMenuButton.this);
                    }
                }

                @Override
                public void cancel() {
                    isDialogShown = false;
                }
            });
            dialog.show(fragmentManager, "formPicker");
            isDialogShown = true;


        }
    };

    @Override
    public boolean validate() {
        return !current_title.isEmpty();
    }

    public static class ChildFormDialogFragment extends DialogFragment {

        private CharSequence[] items_options;
        private PXFSubMenuButton widgetParent;
        private IIndexSelected eventHandler;

        public void setISelectedIndex(IIndexSelected callback) {
            eventHandler = callback;
        }

        public static ChildFormDialogFragment getInstance(PXFSubMenuButton widget) {
            ChildFormDialogFragment cf = new ChildFormDialogFragment();
            cf.widgetParent = widget;
            cf.items_options = widget.options_array;
            return cf;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(widgetParent.getJsonEntries().get(PXWidget.FIELD_TITLE).getValue().getAsString());
            builder.setItems(items_options, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (eventHandler != null) {
                        eventHandler.onIndexSelected(which);
                    }
                }
            });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);

            if (eventHandler != null) {
                eventHandler.cancel();
            }
        }

        public interface IIndexSelected {
            void onIndexSelected(int index);

            void cancel();
        }
    }
}