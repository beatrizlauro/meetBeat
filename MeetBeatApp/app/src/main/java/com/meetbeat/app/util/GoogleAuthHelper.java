package com.meetbeat.app.util;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class GoogleAuthHelper {

    private static final String ERRO_GOOGLE = "Não foi possível entrar com o Google. Tente novamente.";

    // Web Client ID criado no Google Cloud Console (OAuth consent screen).
    // Deve ser o MESMO valor configurado em application.properties (google.oauth.web-client-id) no backend.
    private static final String WEB_CLIENT_ID = "524293189393-1sqq63ainkf55jpvktt8jkkap3vdpmra.apps.googleusercontent.com";

    public interface OnGoogleIdTokenListener {
        void onSuccess(String idToken);
        void onError(String mensagem);
    }

    public static void signIn(Activity activity, OnGoogleIdTokenListener listener) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetSignInWithGoogleOption signInOption =
                new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();

                        if (credential instanceof CustomCredential customCredential
                                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                            try {
                                GoogleIdTokenCredential googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(customCredential.getData());
                                listener.onSuccess(googleIdTokenCredential.getIdToken());
                            } catch (Exception e) {
                                listener.onError(ERRO_GOOGLE);
                            }
                        } else {
                            listener.onError(ERRO_GOOGLE);
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // Fechar a janela de contas do Google é uma escolha da pessoa, não um erro
                        if (e instanceof GetCredentialCancellationException) return;
                        listener.onError(ERRO_GOOGLE);
                    }
                }
        );
    }
}
