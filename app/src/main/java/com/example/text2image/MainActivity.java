package com.example.text2image;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.text2image.api.ApiService;
import com.example.text2image.api.AuthResponse;
import com.example.text2image.api.ImageGenRequest;
import com.example.text2image.api.ImageRequest;
import com.example.text2image.api.ImageResponse;
import com.example.text2image.api.ImageUrlResponse;
import com.example.text2image.api.RetrofitClient;
import com.example.text2image.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Bitmap generatedImageBitmap;
    private static final String API_KEY = "Z3SReVrc5XHnLF3HE1mHSK2-yPXvBQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set app title
        setTitle("Virus Soup AI");

        // Setup UI with animations
        setupAnimatedUI();

        // Handle Generate button click
        binding.btnGenerate.setOnClickListener(v -> {
            generateImage();  // Handles prompt check, auth, and image generation
        });

        // Hook up Save to Gallery button
        binding.btnDownload.setOnClickListener(v -> {
            if (generatedImageBitmap != null) {
                saveImageToGallery(generatedImageBitmap);  // Save image to gallery
            } else {
                Toast.makeText(MainActivity.this, "No image to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Hook up Share Image button
        binding.btnShare.setOnClickListener(v -> {
            if (generatedImageBitmap != null) {
                shareImage(generatedImageBitmap);  // Share image
            } else {
                Toast.makeText(MainActivity.this, "No image to share", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupAnimatedUI() {
        // Start with elements offscreen
        binding.tvHeader.setTranslationY(-100f);
        binding.tvHeader.setAlpha(0f);
        binding.tvSubHeader.setTranslationY(-80f);
        binding.tvSubHeader.setAlpha(0f);
        binding.tilPrompt.setTranslationX(-300f);
        binding.tilPrompt.setAlpha(0f);
        binding.btnGenerate.setScaleX(0f);
        binding.btnGenerate.setScaleY(0f);
        
        // Create animation sequence
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator headerFadeIn = ObjectAnimator.ofFloat(binding.tvHeader, "alpha", 0f, 1f);
        ObjectAnimator headerSlideDown = ObjectAnimator.ofFloat(binding.tvHeader, "translationY", -100f, 0f);
        headerFadeIn.setDuration(800);
        headerSlideDown.setDuration(800);
        headerSlideDown.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ObjectAnimator subheaderFadeIn = ObjectAnimator.ofFloat(binding.tvSubHeader, "alpha", 0f, 1f);
        ObjectAnimator subheaderSlideDown = ObjectAnimator.ofFloat(binding.tvSubHeader, "translationY", -80f, 0f);
        subheaderFadeIn.setDuration(800);
        subheaderSlideDown.setDuration(800);
        subheaderSlideDown.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ObjectAnimator promptFadeIn = ObjectAnimator.ofFloat(binding.tilPrompt, "alpha", 0f, 1f);
        ObjectAnimator promptSlideIn = ObjectAnimator.ofFloat(binding.tilPrompt, "translationX", -300f, 0f);
        promptFadeIn.setDuration(600);
        promptSlideIn.setDuration(600);
        promptSlideIn.setInterpolator(new AccelerateDecelerateInterpolator());
        
        ObjectAnimator btnScaleX = ObjectAnimator.ofFloat(binding.btnGenerate, "scaleX", 0f, 1f);
        ObjectAnimator btnScaleY = ObjectAnimator.ofFloat(binding.btnGenerate, "scaleY", 0f, 1f);
        btnScaleX.setDuration(500);
        btnScaleY.setDuration(500);
        btnScaleX.setInterpolator(new OvershootInterpolator());
        btnScaleY.setInterpolator(new OvershootInterpolator());
        
        // Build sequence
        animatorSet.play(headerFadeIn).with(headerSlideDown);
        animatorSet.play(subheaderFadeIn).with(subheaderSlideDown).after(headerFadeIn);
        animatorSet.play(promptFadeIn).with(promptSlideIn).after(subheaderFadeIn);
        animatorSet.play(btnScaleX).with(btnScaleY).after(promptFadeIn);
        
        // Start animation
        animatorSet.start();
    }

    private void generateImage() {
        String prompt = binding.etPrompt.getText().toString().trim();

        if (TextUtils.isEmpty(prompt)) {
            binding.etPrompt.setError("Please enter a prompt");
            binding.etPrompt.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        String API_KEY = "c0957e34a11786192e8819a7d4faef725c3a0becf05716823b30e37111196e92ba1953a695dddd761cce8abbffefce40da8059d06aa651a02f9cc3322a7d1e0b";
        // Step 1: Authenticate
        apiService.authenticate(API_KEY).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String signature = response.body().getSignature();

                    // Step 2: Generate Image using signature
                    ImageGenRequest request = new ImageGenRequest(signature, prompt);
                    apiService.generateImage(
                            API_KEY,
                            request
                    ).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            binding.progressBar.setVisibility(View.GONE);
                            Log.i("RESPONSE BODY", response.body().toString());
                            if (response.isSuccessful() && response.body() != null) {

                                String imageUrl = response.body().replace("\"", "");

                                loadImageFromUrl("https://ai.elliottwen.info/" +imageUrl);
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to generate image.", Toast.LENGTH_SHORT).show();
                            }
                        }


                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Image generation error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Auth failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Auth error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadImageFromUrl(String imageUrl) {
        // Make UI elements visible but with alpha=0 for animation
        binding.tvResultLabel.setVisibility(View.VISIBLE);
        binding.tvResultLabel.setAlpha(0f);
        
        binding.cardImage.setVisibility(View.VISIBLE);
        binding.cardImage.setAlpha(0f);
        binding.cardImage.setScaleX(0.8f);
        binding.cardImage.setScaleY(0.8f);
        
        binding.btnDownload.setVisibility(View.VISIBLE);
        binding.btnDownload.setAlpha(0f);
        binding.btnDownload.setTranslationY(50f);
        
        binding.btnShare.setVisibility(View.VISIBLE);
        binding.btnShare.setAlpha(0f);
        binding.btnShare.setTranslationY(50f);

        // Load image with Glide
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.ivGeneratedImage.setImageBitmap(resource);
                        generatedImageBitmap = resource;
                        
                        // Animate the appearance of the image and controls
                        animateImageAppearance();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }
                });
    }
    
    private void animateImageAppearance() {
        // Fade in animation for the label with glow effect
        ObjectAnimator labelFadeIn = ObjectAnimator.ofFloat(binding.tvResultLabel, "alpha", 0f, 1f);
        labelFadeIn.setDuration(400);
        
        // Scale and fade animation for the image card
        ObjectAnimator cardScaleX = ObjectAnimator.ofFloat(binding.cardImage, "scaleX", 0.8f, 1f);
        ObjectAnimator cardScaleY = ObjectAnimator.ofFloat(binding.cardImage, "scaleY", 0.8f, 1f);
        ObjectAnimator cardFadeIn = ObjectAnimator.ofFloat(binding.cardImage, "alpha", 0f, 1f);
        cardScaleX.setDuration(700);
        cardScaleY.setDuration(700);
        cardFadeIn.setDuration(600);
        
        // Set interpolator for bounce effect
        cardScaleX.setInterpolator(new OvershootInterpolator(1.5f));
        cardScaleY.setInterpolator(new OvershootInterpolator(1.5f));
        
        // Button animations with slide-up effect
        ObjectAnimator btnDownloadFadeIn = ObjectAnimator.ofFloat(binding.btnDownload, "alpha", 0f, 1f);
        ObjectAnimator btnShareFadeIn = ObjectAnimator.ofFloat(binding.btnShare, "alpha", 0f, 1f);
        ObjectAnimator btnDownloadSlideUp = ObjectAnimator.ofFloat(binding.btnDownload, "translationY", 50f, 0f);
        ObjectAnimator btnShareSlideUp = ObjectAnimator.ofFloat(binding.btnShare, "translationY", 50f, 0f);
        
        btnDownloadFadeIn.setDuration(500);
        btnShareFadeIn.setDuration(500);
        btnDownloadSlideUp.setDuration(500);
        btnShareSlideUp.setDuration(500);
        
        btnDownloadSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
        btnShareSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Create animation set and play animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(labelFadeIn).before(cardFadeIn);
        animatorSet.play(cardFadeIn).with(cardScaleX).with(cardScaleY);
        animatorSet.play(btnDownloadFadeIn).with(btnShareFadeIn)
                   .with(btnDownloadSlideUp).with(btnShareSlideUp)
                   .after(cardFadeIn);
        
        animatorSet.start();
    }

    private void saveImageToGallery(Bitmap bitmap) {
        OutputStream fos;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "VirusSoup_" + timeStamp + ".jpg";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VirusSoup");
            
            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            
            try {
                fos = getContentResolver().openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                
                // Animate success feedback
                animateSuccessEffect(binding.btnDownload);
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // For devices below Android Q
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "VirusSoup");

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, imageFileName);
            try {
                fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                // Notify gallery app
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                // Animate success feedback
                animateSuccessEffect(binding.btnDownload);
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void animateSuccessEffect(View view) {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f);
        
        scaleUpX.setDuration(200);
        scaleUpY.setDuration(200);
        scaleDownX.setDuration(200);
        scaleDownY.setDuration(200);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleUpX).with(scaleUpY);
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX);
        animatorSet.start();
    }

    private void shareImage(Bitmap bitmap) {
        try {
            // Save bitmap to cache directory
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "shared_image.jpg");
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

            // Get FileProvider URI
            Uri imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider", imageFile);

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Animate success feedback
            animateSuccessEffect(binding.btnShare);
            
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 