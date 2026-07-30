package com.meetbeat.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.meetbeat.app.LoginActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.util.UserSession;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        view.findViewById(R.id.btnSairPerfil).setOnClickListener(v -> {
            UserSession.logout(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}