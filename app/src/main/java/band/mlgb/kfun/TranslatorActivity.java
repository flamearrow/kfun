package band.mlgb.kfun;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import band.mlgb.kfun.inject.DaggerFirebaseComponent;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class TranslatorActivity extends InputTextActivity {
    @Inject
    FirebaseTranslator zhToEnTranslator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerFirebaseComponent.create().inject(this);
        Button b = findViewById(R.id.fire);
        EditText e = findViewById(R.id.input);

        b.setEnabled(false);
        e.setEnabled(false);
        e.setText("Downloading language model...");
        zhToEnTranslator.downloadModelIfNeeded().addOnSuccessListener(
                v -> {
                    b.setEnabled(true);
                    e.setEnabled(true);
                    e.setText("");
                    toastShort("Language model downloaded!");
                }
        ).addOnFailureListener(
                exception ->
                        toastShort("Failed to download model.")
        );
    }

    @NotNull
    @Override
    public String buttonText() {
        return "Translate";
    }

    @Nullable
    @Override
    public String hintText() {
        return "Input ZH, get EN!";
    }

    @Override
    public void handleText(@NotNull String input) {
        zhToEnTranslator.translate(input).addOnSuccessListener(this::postResult
        ).addOnFailureListener(e ->
                toastShort(e.getLocalizedMessage())
        );
    }
}
