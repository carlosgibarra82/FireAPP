package com.optic.fireapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.optic.fireapp.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullScreenDialog extends DialogFragment {

    // REFERENCIAR VISTAS
    @BindView(R.id.imageView) ImageView mImageView;

    private String mImagen = "";

    /**
     * CONTRUCTOR
     */
    public static FullScreenDialog newInstance (String imagen) {

        // RECIBIR URL DE LA IMAGEN Y EL PRECIO
        FullScreenDialog f = new FullScreenDialog();
        Bundle args = new Bundle();
        args.putString("imagen", imagen);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
        // OBTENER PARAMETROS ENVIADOS CUANDO SE ABRE LA IMAGEN
        mImagen = getArguments().getString("imagen");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_dialog, container, false);
        ButterKnife.bind(this, view);
        if (mImagen != null) {
            if (!mImagen.equals("")) {
                Picasso.with(getContext()).load(mImagen).into(mImageView);
            }
            else {
                Toast.makeText(getContext(), "No se puede cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), "No se puede cargar la imagen", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    /**
     * CLICK CERRAR VENTANA
     */
    @OnClick(R.id.linearLayoutClose)
    void onClickClose() {
        dismiss();
    }

}
