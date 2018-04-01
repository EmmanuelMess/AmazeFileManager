package com.amaze.filemanager.fragments.preference_fragments;

/**
 * @author Emmanuel Messulam <emmanuelbendavid@gmail.com>
 *         on 1/1/2018, at 21:16.
 */

public class PreferencesConstants {
    //START fragments
    public static final String FRAGMENT_THEME = "theme";
    public static final String FRAGMENT_COLORS = "colors";
    public static final String FRAGMENT_FOLDERS = "sidebar_folders";
    public static final String FRAGMENT_QUICKACCESSES = "sidebar_quickaccess";
    public static final String FRAGMENT_ADVANCED_SEARCH = "advancedsearch";
    public static final String FRAGMENT_ABOUT = "about";
    public static final String FRAGMENT_FEEDBACK = "feedback";
    //END fragments

    //START preferences.xml constants
    public static final String PREFERENCE_INTELLI_HIDE_TOOLBAR = "intelliHideToolbar";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_FILE_SIZE = "showFileSize";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_PERMISSIONS = "showPermissions";
    public static final String PREFERENCE_SHOW_DIVIDERS = "showDividers";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_HEADERS = "showHeaders";
    public static final String PREFERENCE_SHOW_GOBACK_BUTTON = "goBack_checkbox";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_SIDEBAR_FOLDERS = "sidebar_folders_enable";
    public static final String PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES = "sidebar_quickaccess_enable";

    public static final String PREFERENCE_BOOKMARKS_ADDED = "books_added";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_TEXTEDITOR_NEWSTACK = "texteditor_newstack";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_HIDDENFILES = "showHidden";
    public static final String PREFERENCE_SHOW_LAST_MODIFIED = "showLastModified";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_USE_CIRCULAR_IMAGES = "circularimages";
    public static final String PREFERENCE_ROOTMODE = "rootmode";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_CHANGEPATHS = "typeablepaths";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_GRID_COLUMNS = "columns";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SHOW_THUMB = "showThumbs";// TODO: 23/03/18 update dynamically

    public static final String PREFERENCE_CRYPT_MASTER_PASSWORD = "crypt_password";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_CRYPT_FINGERPRINT = "crypt_fingerprint";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_CRYPT_WARNING_REMEMBER = "crypt_remember";// TODO: 23/03/18 update dynamically

    public static final String ENCRYPT_PASSWORD_FINGERPRINT = "fingerprint";// TODO: 23/03/18 update dynamically
    public static final String ENCRYPT_PASSWORD_MASTER = "master";// TODO: 23/03/18 update dynamically

    public static final String PREFERENCE_CRYPT_MASTER_PASSWORD_DEFAULT = "";// TODO: 23/03/18 update dynamically
    public static final boolean PREFERENCE_CRYPT_FINGERPRINT_DEFAULT = false;// TODO: 23/03/18 update dynamically
    public static final boolean PREFERENCE_CRYPT_WARNING_REMEMBER_DEFAULT = false;// TODO: 23/03/18 update dynamically
    //END preferences.xml constants

    //START color_prefs.xml constants
    public static final String PREFERENCE_SKIN = "skin";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_SKIN_TWO = "skin_two";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_ACCENT = "accent_skin";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_ICON_SKIN = "icon_skin";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_COLORIZE_ICONS = "coloriseIcons";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_COLORED_NAVIGATION = "colorednavigation";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_RANDOM_COLOR = "random_checkbox";// TODO: 23/03/18 update dynamically
    //END color_prefs.xml constants

    //START folders_prefs.xml constants
    public static final String PREFERENCE_SHORTCUT = "add_shortcut";// TODO: 23/03/18 update dynamically
    //END folders_prefs.xml constants

    //START random preferences
    public static final String PREFERENCE_DIRECTORY_SORT_MODE = "dirontop";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_DRAWER_HEADER_PATH = "drawer_header_path";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_URI = "URI";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_HIDEMODE = "hidemode";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_VIEW = "view";// TODO: 23/03/18 update dynamically
    public static final String PREFERENCE_NEED_TO_SET_HOME = "needtosethome";// TODO: 23/03/18 update dynamically

    /**
     * The value is an int with values RANDOM_INDEX, CUSTOM_INDEX, NO_DATA or [0, ...]
     */
    public static final String PREFERENCE_COLOR_CONFIG = "color config";// TODO: 23/03/18 update dynamically
    //END random preferences
}
