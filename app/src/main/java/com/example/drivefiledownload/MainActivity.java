package com.example.drivefiledownload;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import com.download.drive.file.GoogleDriveConfig;
import com.download.drive.file.GoogleDriveService;
import com.download.drive.file.ServiceListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class MainActivity extends AppCompatActivity implements ServiceListener {

    private Button login, drive, logout;
    private GoogleDriveService googleDriveService;
    private GoogleDriveConfig googleDriveConfig;

    enum ButtonState {
        LOGGED_OUT, LOGGED_IN
    }

    private ButtonState state = ButtonState.LOGGED_OUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.login);
        drive = findViewById(R.id.start);
        logout = findViewById(R.id.logout);

        googleDriveConfig = new GoogleDriveConfig(getString(R.string.app_name),
                GoogleDriveService.Companion.getDocumentMimeTypes());
        googleDriveService = new GoogleDriveService(this, googleDriveConfig);

        googleDriveService.setServiceListener(this);
        googleDriveService.checkLoginStatus();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionLogIn();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionLogOut();
            }
        });

        drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionDrive();
            }
        });

//        setButtons();
    }

    private void actionLogIn() {
        googleDriveService.auth();
    }

    private void actionLogOut() {
        googleDriveService.logout();
        state = ButtonState.LOGGED_OUT;
        setButtons();
    }

    private void actionDrive() {
        googleDriveService.pickFiles(null);
    }

    private void setButtons() {
        switch (state) {
            case LOGGED_IN:
                login.setEnabled(true);
                drive.setEnabled(true);
                logout.setEnabled(false);
                break;

            case LOGGED_OUT:
                login.setEnabled(false);
                drive.setEnabled(false);
                logout.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        googleDriveService.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void loggedIn() {
        state = ButtonState.LOGGED_IN;
        setButtons();
    }

    @Override
    public void fileDownloaded(@NotNull File file) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Uri uri = Uri.fromFile(file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        intent.setDataAndType(apkURI, mimeType);
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.v("MIMO_SAHA: ", "Not Found");
        }
    }

    @Override
    public void cancelled() {
        Log.v("MIMO_SAHA: ", "Canceled");
    }

    @Override
    public void handleError(@NotNull Exception exception) {
        Log.v("MIMO_SAHA: ", "Exception: " + exception);
    }
}
