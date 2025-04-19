package com.example.text2image.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.example.text2image.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleAnimationView extends View {
    
    private static final int PARTICLE_COUNT = 120;
    private static final int DIGITAL_RAIN_COUNT = 20;
    private static final int GLOW_SPOTS = 5;
    private static final int REFRESH_RATE = 16; // ~60fps
    
    private final List<Particle> particles = new ArrayList<>();
    private final List<DigitalRain> digitalRains = new ArrayList<>();
    private final List<GlowSpot> glowSpots = new ArrayList<>();
    private final Random random = new Random();
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            updateAnimations();
            invalidate();
            handler.postDelayed(this, REFRESH_RATE);
        }
    };
    
    private int primaryColor;
    private int accentColor;
    private int width;
    private int height;
    private boolean isAnimating = false;
    private long startTime;

    public ParticleAnimationView(Context context) {
        super(context);
        init();
    }

    public ParticleAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        primaryColor = getResources().getColor(R.color.colorPrimary);
        accentColor = getResources().getColor(R.color.colorAccent);
        
        // Configure paint
        particlePaint.setStyle(Paint.Style.FILL);
        rainPaint.setStyle(Paint.Style.FILL);
        rainPaint.setTextSize(24f);
        rainPaint.setColor(primaryColor);
        glowPaint.setStyle(Paint.Style.FILL);
        
        startTime = System.currentTimeMillis();
        
        setLayerType(LAYER_TYPE_HARDWARE, null); // Use hardware acceleration
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        // Initialize animations when size is known
        initParticles();
        initDigitalRain();
        initGlowSpots();
    }
    
    private void initParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createRandomParticle());
        }
    }
    
    private void initDigitalRain() {
        digitalRains.clear();
        for (int i = 0; i < DIGITAL_RAIN_COUNT; i++) {
            digitalRains.add(createDigitalRain());
        }
    }
    
    private void initGlowSpots() {
        glowSpots.clear();
        for (int i = 0; i < GLOW_SPOTS; i++) {
            glowSpots.add(createGlowSpot());
        }
    }
    
    private Particle createRandomParticle() {
        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        float size = 1.5f + random.nextFloat() * 3.5f;
        float speedX = -0.5f + random.nextFloat();
        float speedY = -0.5f + random.nextFloat() * 0.5f; // Some go up, some go down
        int alpha = 40 + random.nextInt(150);
        int color = random.nextInt(2) == 0 ? primaryColor : accentColor;
        
        return new Particle(x, y, size, speedX, speedY, alpha, color);
    }
    
    private DigitalRain createDigitalRain() {
        float x = random.nextFloat() * width;
        float y = -100 - random.nextFloat() * 400; // Start above screen
        float speed = 2f + random.nextFloat() * 6f;
        int length = 5 + random.nextInt(25);
        float alpha = 0.2f + random.nextFloat() * 0.7f;
        
        return new DigitalRain(x, y, speed, length, alpha);
    }
    
    private GlowSpot createGlowSpot() {
        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        float radius = 150f + random.nextFloat() * 250f;
        float alpha = 0.05f + random.nextFloat() * 0.1f; // Subtle glow
        float duration = 5000 + random.nextFloat() * 10000; // 5-15 seconds per pulse
        float pulseFactor = 0.6f + random.nextFloat() * 0.8f;
        
        return new GlowSpot(x, y, radius, alpha, pulseFactor, duration);
    }
    
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) / 1000f;
        
        // Update particles
        for (Particle particle : particles) {
            // Add subtle waving motion
            particle.x += particle.speedX + (float)Math.sin(elapsedTime * 0.5f + particle.y * 0.01f) * 0.2f;
            particle.y += particle.speedY;
            
            // Wrap around screen
            if (particle.x < 0) particle.x = width;
            if (particle.x > width) particle.x = 0;
            if (particle.y < 0) particle.y = height;
            if (particle.y > height) particle.y = 0;
            
            // Random alpha fluctuation for twinkling effect
            particle.alpha += random.nextInt(20) - 10;
            if (particle.alpha < 50) particle.alpha = 50;
            if (particle.alpha > 200) particle.alpha = 200;
        }
        
        // Update digital rain
        for (DigitalRain rain : digitalRains) {
            rain.y += rain.speed;
            
            // Reset when it goes off screen
            if (rain.y > height + 300) {
                rain.x = random.nextFloat() * width;
                rain.y = -100 - random.nextFloat() * 200;
                rain.speed = 2f + random.nextFloat() * 6f;
                rain.length = 5 + random.nextInt(25);
                rain.alpha = 0.2f + random.nextFloat() * 0.7f;
            }
        }
        
        // Update glow spots
        for (GlowSpot spot : glowSpots) {
            // Calculate pulse based on time
            float pulseCycle = (currentTime % (long)spot.duration) / spot.duration;
            float pulseValue = (float)Math.abs(Math.sin(pulseCycle * Math.PI));
            spot.currentAlpha = spot.alpha * (spot.pulseFactor + pulseValue * (1f - spot.pulseFactor));
            
            // Slowly drift position
            spot.x += (random.nextFloat() - 0.5f) * 0.5f;
            spot.y += (random.nextFloat() - 0.5f) * 0.5f;
            
            // Keep on screen
            if (spot.x < 0) spot.x = 0;
            if (spot.x > width) spot.x = width;
            if (spot.y < 0) spot.y = 0;
            if (spot.y > height) spot.y = height;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw glow spots (behind everything)
        for (GlowSpot spot : glowSpots) {
            glowPaint.setShader(new RadialGradient(
                spot.x, spot.y, spot.radius,
                Color.argb((int)(spot.currentAlpha * 255), 0, 255, 65),
                Color.argb(0, 0, 255, 65),
                Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(spot.x, spot.y, spot.radius, glowPaint);
            glowPaint.setShader(null);
        }
        
        // Draw particles
        for (Particle particle : particles) {
            int color = particle.color;
            particlePaint.setColor(Color.argb(particle.alpha, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);
        }
        
        // Draw digital rain
        for (DigitalRain rain : digitalRains) {
            for (int i = 0; i < rain.length; i++) {
                // Digital rain characters get more transparent as they go down
                float charAlpha = 1.0f - ((float)i / rain.length);
                rainPaint.setAlpha((int)(rain.alpha * charAlpha * 255));
                
                // First character is brighter
                if (i == 0) {
                    rainPaint.setAlpha((int)(rain.alpha * 255));
                }
                
                // Choose a random character from our digital set
                char c = getRandomDigitalChar(rain.x, rain.y, i);
                canvas.drawText(String.valueOf(c), rain.x, rain.y - (i * 20), rainPaint);
            }
        }
    }
    
    private char getRandomDigitalChar(float x, float y, int position) {
        // For consistent characters in each position of a stream
        if (position == 0 || random.nextInt(30) == 0) {
            // Matrix-style characters: mix of numbers, symbols and letters
            String chars = "01零一ﾊﾐﾋｰｳｼﾅﾓﾆｻﾜﾂｵﾘｱﾎﾃﾏｹﾒｴｶｷﾑﾕﾗｾﾈｽﾀﾇﾍ日十字";
            return chars.charAt(random.nextInt(chars.length()));
        } else {
            // Use location-based hash for consistent characters per position
            int hashCode = ((int)x * 31 + (int)y * 17 + position * 13) % 36;
            if (hashCode < 10) {
                return (char)('0' + hashCode); // 0-9
            } else {
                return (char)('a' + (hashCode - 10)); // a-z
            }
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isAnimating) {
            isAnimating = true;
            startTime = System.currentTimeMillis();
            handler.post(animator);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAnimating = false;
        handler.removeCallbacks(animator);
    }
    
    // Particle class to track position, speed and color
    private static class Particle {
        float x, y;
        float size;
        float speedX, speedY;
        int alpha;
        int color;
        
        Particle(float x, float y, float size, float speedX, float speedY, int alpha, int color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speedX = speedX;
            this.speedY = speedY;
            this.alpha = alpha;
            this.color = color;
        }
    }
    
    // Digital rain similar to The Matrix
    private static class DigitalRain {
        float x, y;
        float speed;
        int length;
        float alpha;
        
        DigitalRain(float x, float y, float speed, int length, float alpha) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.length = length;
            this.alpha = alpha;
        }
    }
    
    // Ambient glow spots
    private static class GlowSpot {
        float x, y;
        float radius;
        float alpha;
        float currentAlpha;
        float pulseFactor;
        float duration;
        
        GlowSpot(float x, float y, float radius, float alpha, float pulseFactor, float duration) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.alpha = alpha;
            this.currentAlpha = alpha;
            this.pulseFactor = pulseFactor;
            this.duration = duration;
        }
    }
} 