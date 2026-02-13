import android.content.Context

object VoxPrefs {

    private const val PREF = "vox_prefs"
    private const val KEY_ENABLED = "assistant_enabled"

    fun isEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, value: Boolean) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_ENABLED, value).apply()
    }
}
