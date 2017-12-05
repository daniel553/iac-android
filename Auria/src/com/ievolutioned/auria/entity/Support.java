package com.ievolutioned.auria.entity;

/**
 * Created by Daniel on 17/04/2017.
 */

public class Support {
    public abstract static class Category {
        public final static String NORMAL = "NORMAL";
        public final static String NO_SUPPORT = "SIN SUBSIDIO";
        public final static String EXTRA_TIME = "TIEMPO EXTRA";
        public final static String GUEST = "INVITADO";

        private final static String NORMAL_ID = "normal";
        private final static String NO_SUPPORT_ID = "subsidy";
        private final static String EXTRA_TIME_ID = "extra_time";
        private final static String GUEST_ID = "guest";

        public final static long GUEST_ID_DEFAULT = 0;
        public final static String GUEST_DEFAULT_IAC_ID = "";

        public static String getSupportCategoryId(final String category) {
            switch (category) {
                case NORMAL:
                    return NORMAL_ID;
                case NO_SUPPORT:
                    return NO_SUPPORT_ID;
                case EXTRA_TIME:
                    return EXTRA_TIME_ID;
                case GUEST:
                    return GUEST_ID;
                default:
                    return NO_SUPPORT_ID;
            }
        }

        public static String getSupportCategory(final String categoryId) {
            switch (categoryId) {
                case NORMAL_ID:
                    return NORMAL;
                case NO_SUPPORT_ID:
                    return NO_SUPPORT;
                case EXTRA_TIME_ID:
                    return EXTRA_TIME;
                case GUEST_ID:
                    return GUEST;
                default:
                    return NO_SUPPORT;
            }
        }
    }

    public abstract static class Type {
        public final static String FOOD = "COMIDA";
        public final static String BEVERAGE = "REFRESCO";
        public final static String WATER = "AGUA";

        private final static String FOOD_ID = "food";
        private final static String BEVERAGE_ID = "soda";
        private final static String WATER_ID = "water";

        public static String getSupportTypeId(final String type) {
            switch (type) {
                case FOOD:
                    return FOOD_ID;
                case BEVERAGE:
                    return BEVERAGE_ID;
                case WATER:
                    return WATER_ID;
                default:
                    return FOOD_ID;
            }
        }

        public static String getSupportType(final String typeId) {
            switch (typeId) {
                case FOOD_ID:
                    return FOOD;
                case BEVERAGE_ID:
                    return BEVERAGE;
                case WATER_ID:
                    return WATER;
                default:
                    return FOOD;
            }
        }
    }
}
