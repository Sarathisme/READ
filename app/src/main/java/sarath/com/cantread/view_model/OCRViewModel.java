package sarath.com.cantread.view_model;

import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class OCRViewModel extends ViewModel {

    private static final String DEBUG_TAG = "OCRViewModel Debug";
    private static final String ERROR_TAG = "OCRViewModel Error";

    public OCRViewModel() {
    }

    public FirebaseVisionImage createFirebaseVisionImage(Bitmap mediaImage) {
        return FirebaseVisionImage.fromBitmap(mediaImage);
    }

    public Task<FirebaseVisionText> process(FirebaseVisionImage image) {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        return detector.processImage(image);
    }
}