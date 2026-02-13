package com.voxcom.vox.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.voxcom.vox.system.VoxPrefs

class VoxTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val enabled = VoxPrefs.isEnabled(this)

        if (enabled) {
            // turn OFF
            VoxPrefs.setEnabled(this, false)
            stopService(Intent(this, VoxService::class.java))
            qsTile.state = Tile.STATE_INACTIVE

        } else {
            // turn ON
            VoxPrefs.setEnabled(this, true)

            val intent = Intent(this, VoxService::class.java)
            intent.action = "VOX_WAKE"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)

            qsTile.state = Tile.STATE_ACTIVE
        }

        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.state =
            if (VoxPrefs.isEnabled(this))
                Tile.STATE_ACTIVE
            else
                Tile.STATE_INACTIVE

        qsTile.updateTile()
    }
}