package com.example.share

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val appPerms = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            activityResultLauncher.launch(appPerms)
        }
    }

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                copiaArquivoAssentTOsd()
                share()
            }
        }

    fun copiaArquivoAssentTOsd() {
        try {
            val inStream = resources.assets.open("teste.pdf")
            val outStream =
                FileOutputStream(localArquivo())

            val buffer = ByteArray(1024)
            var length = inStream.read(buffer)


            while (length > 0) {
                outStream.write(buffer, 0, length)
                length = inStream.read(buffer)
            }

            inStream.close()
            outStream.close()
            println("Copiado com sucesso. " + localArquivo().absoluteFile)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun localArquivo(): File {
        val nomeDoArquivo = "tecnologia"
        val pasta = "pdf"

        val diretorio = this.getExternalFilesDir(null)?.absolutePath + "/$pasta/"

        val file = File(diretorio)

        if (!file.exists()) {
            file.mkdirs()
        }

        val targetPdf = "$diretorio$nomeDoArquivo.pdf"

        return File(targetPdf)
    }

    fun uriArquivo(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                this,
                this.packageName + ".provider",
                localArquivo()
            )
        } else {
            Uri.fromFile(localArquivo())
        }
    }

    fun share(){
        var intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        val uri = uriArquivo()
        intent.putExtra(Intent.EXTRA_STREAM,uri)
        val chooser = Intent.createChooser(intent,"Compartilhar")

        val resInfoList = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(chooser)
    }
}