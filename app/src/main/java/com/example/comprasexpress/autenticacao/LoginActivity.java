package com.example.comprasexpress.autenticacao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comprasexpress.activity.loja.MainActivityLoja;
import com.example.comprasexpress.activity.usuario.MainActivityUsuario;
import com.example.comprasexpress.databinding.ActivityLoginBinding;
import com.example.comprasexpress.FireHelper.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String email = result.getData().getStringExtra("email");
                    binding.edtEmail.setText(email);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configClicks();
    }

    public void validaDados(View view) {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {

                binding.progressBar.setVisibility(View.VISIBLE);

                login(email, senha);

            } else {
                binding.edtSenha.requestFocus();
                binding.edtSenha.setError("Informe uma senha.");
            }
        } else {
            binding.edtEmail.requestFocus();
            binding.edtEmail.setError("Informe seu email.");
        }

    }

    private void login(String email, String senha) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(
                email, senha
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recuperaUsuario(task.getResult().getUser().getUid());
                finish();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, FirebaseHelper.validaErros(task.getException().getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recuperaUsuario(String id) {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(id);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){ // Usuário
                    finish();
                    startActivity(new Intent(getBaseContext(), MainActivityUsuario.class));
                }else { // Loja
                    finish();
                    startActivity(new Intent(getBaseContext(), MainActivityLoja.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configClicks() {
        binding.include.ibVoltar.setOnClickListener(view -> finish());

        binding.btnRecuperaSenha.setOnClickListener(view ->
                startActivity(new Intent(this, RecuperaContaActivity.class)));

        binding.btnCadastro.setOnClickListener(view -> {
            Intent intent = new Intent(this, CadastroActivity.class);
            resultLauncher.launch(intent);
        });
    }
}