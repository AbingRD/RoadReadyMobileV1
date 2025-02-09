package com.example.roadready.fragments.buyer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.roadready.R;
import com.example.roadready.classes.general.MainFacade;
import com.example.roadready.classes.general.RoadReadyServer;
import com.example.roadready.classes.model.gson.GsonData;
import com.example.roadready.classes.model.gson.data.GoogleAuthGson;
import com.example.roadready.classes.model.gson.response.SuccessGson;
import com.example.roadready.databinding.FragmentBuyerSignUpBinding;

public class Buyer_SignUp_Fragment extends Fragment {
    private final String TAG = "SignUp_Fragment";
    private FragmentBuyerSignUpBinding binding;
    private MainFacade mainFacade;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBuyerSignUpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        try {
            mainFacade = MainFacade.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mainFacade.hideProgressBar();
        mainFacade.hideBackDrop();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initActions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void initActions() {
        binding.sgnupBtnSubmit.setOnClickListener(v -> {
            processRegistration();
        });

        binding.sgnupTextLogin.setOnClickListener(v -> {
            mainFacade.getCommonMainNavController().navigate(R.id.action_global_login_Fragment);
        });

        binding.sgnupBtnGoogleLogin.setOnClickListener(v -> {
            processGoogleAuth();
        });

        binding.sgnupChkTogglePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.sgnupInptPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.sgnupInptPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.sgnupInptPassword.setSelection(binding.sgnupInptPassword.getText().length());
        });
    }

    private void processGoogleAuth() {
        mainFacade.showBackDrop();
        mainFacade.showProgressBar();
        mainFacade.getGoogleAuthLink(new RoadReadyServer.ResponseListener<GoogleAuthGson>() {
            @Override
            public void onSuccess(SuccessGson<GoogleAuthGson> response) {
                String authenticationUrl = response.getData().getAuthorizationUrl();

                Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authenticationUrl));
                startActivity(googleIntent);
                mainFacade.hideBackDrop();
                mainFacade.hideProgressBar();
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                mainFacade.hideBackDrop();
                mainFacade.hideProgressBar();
            }
        });
    }

    private void processRegistration() {
        showProgressBar();

        String email = String.valueOf(binding.sgnupInptEmail.getText());
        String password = passwordCheck(String.valueOf(binding.sgnupInptPassword.getText()));
        String firstName = String.valueOf(binding.sgnupInptFname.getText());
        String lastName = String.valueOf(binding.sgnupInptLname.getText());
        String phoneNumber = phoneNumberCheck(String.valueOf(binding.sgnupInptPhoneNumber.getText()));
        String gender = getGender();
        String address = String.valueOf(binding.sgnupInptAddress.getText());

        final RoadReadyServer.ResponseListener<GsonData> responseListener = new RoadReadyServer.ResponseListener<GsonData>() {
            @Override
            public void onSuccess(SuccessGson<GsonData> response) {
                mainFacade.makeToast(response.getMessage(), Toast.LENGTH_SHORT);
                hideProgressBar();
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                hideProgressBar();
            }
        };

        mainFacade.registerBuyer(responseListener, email, password, firstName, lastName, phoneNumber, gender, address);
    }

    private String passwordCheck(String pass){
        if(pass.length() < 8){
            mainFacade.makeToast("Password must be at least 8 characters long", Toast.LENGTH_SHORT);
            return "";
        }
        return pass;
    }

    private String phoneNumberCheck(String phoneno){
        if(phoneno.length() != 11){
            mainFacade.makeToast("Please enter an 11-digit phone number", Toast.LENGTH_SHORT);
            return "";
        }
        if(phoneno.charAt(0) != '0' || phoneno.charAt(1) != '9'){
            mainFacade.makeToast("Please enter a valid phone number", Toast.LENGTH_SHORT);
            return "";
        }
        return phoneno;
    }

    private String getGender(){
        if(binding.sgnupRgSexOptions.getCheckedRadioButtonId() != -1){
            return String.valueOf(binding.getRoot().findViewById(binding.sgnupRgSexOptions.getCheckedRadioButtonId()).getContentDescription());
        }
        return "";
    }

    private void showProgressBar() {
        binding.sgnupBtnSubmit.setEnabled(false);
        mainFacade.showProgressBar();
    }

    private void hideProgressBar() {
        binding.sgnupBtnSubmit.setEnabled(true);
        mainFacade.hideProgressBar();
    }
}
