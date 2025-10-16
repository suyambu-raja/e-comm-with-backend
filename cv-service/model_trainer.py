"""
Advanced Product Verification Model Training Script
This script trains custom models for product authenticity detection
"""

import tensorflow as tf
import numpy as np
import cv2
from PIL import Image
import os
import json
import requests
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
import matplotlib.pyplot as plt
from datetime import datetime
import logging
from typing import List, Tuple, Dict
import pickle

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ProductVerificationTrainer:
    def __init__(self, google_api_key: str):
        self.google_api_key = google_api_key
        self.model = None
        self.training_history = None
        self.class_weights = None
        
    def create_advanced_model(self, input_shape=(224, 224, 3)) -> tf.keras.Model:
        """Create an advanced CNN model for product verification"""
        
        # Use a pre-trained ResNet50 as base
        base_model = tf.keras.applications.ResNet50(
            weights='imagenet',
            include_top=False,
            input_shape=input_shape
        )
        
        # Freeze first layers, fine-tune last layers
        for layer in base_model.layers[:-20]:
            layer.trainable = False
        
        # Add custom classification head
        model = tf.keras.Sequential([
            base_model,
            tf.keras.layers.GlobalAveragePooling2D(),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dense(512, activation='relu'),
            tf.keras.layers.Dropout(0.5),
            tf.keras.layers.Dense(256, activation='relu'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dropout(0.3),
            tf.keras.layers.Dense(128, activation='relu'),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(1, activation='sigmoid', name='authenticity_score')
        ])
        
        # Compile with advanced optimizer
        optimizer = tf.keras.optimizers.Adam(
            learning_rate=0.0001,
            beta_1=0.9,
            beta_2=0.999,
            epsilon=1e-07
        )
        
        model.compile(
            optimizer=optimizer,
            loss='binary_crossentropy',
            metrics=['accuracy', 'precision', 'recall']
        )
        
        logger.info(f"Model created with {model.count_params()} parameters")
        return model
    
    def generate_synthetic_training_data(self, num_samples: int = 1000) -> Tuple[np.ndarray, np.ndarray]:
        """Generate synthetic training data for demonstration"""
        logger.info(f"Generating {num_samples} synthetic training samples...")
        
        X = []
        y = []
        
        for i in range(num_samples):
            # Create synthetic product images
            if i % 2 == 0:  # Authentic products
                # Higher quality, consistent features
                img = self._create_authentic_product_image()
                label = 1
            else:  # Fake products
                # Lower quality, inconsistent features
                img = self._create_fake_product_image()
                label = 0
            
            X.append(img)
            y.append(label)
            
            if (i + 1) % 100 == 0:
                logger.info(f"Generated {i + 1}/{num_samples} samples")
        
        return np.array(X), np.array(y)
    
    def _create_authentic_product_image(self) -> np.ndarray:
        """Create a synthetic authentic product image"""
        # High quality, consistent branding
        img = np.random.rand(224, 224, 3) * 0.3 + 0.7  # Brighter, cleaner
        
        # Add consistent patterns (simulating good branding)
        img[:50, :50] = [0.8, 0.2, 0.2]  # Consistent logo area
        img[100:150, 100:150] = [0.1, 0.1, 0.9]  # Clear text area
        
        # Add some noise but keep it minimal
        noise = np.random.normal(0, 0.02, img.shape)
        img = np.clip(img + noise, 0, 1)
        
        return img.astype(np.float32)
    
    def _create_fake_product_image(self) -> np.ndarray:
        """Create a synthetic fake product image"""
        # Lower quality, inconsistent features
        img = np.random.rand(224, 224, 3) * 0.8 + 0.1  # Darker, less clear
        
        # Add inconsistent patterns (simulating poor counterfeiting)
        img[:50, :50] = np.random.rand(50, 50, 3) * 0.5  # Inconsistent logo
        img[100:150, 100:150] = np.random.rand(50, 50, 3) * 0.7  # Blurry text
        
        # Add more noise
        noise = np.random.normal(0, 0.1, img.shape)
        img = np.clip(img + noise, 0, 1)
        
        return img.astype(np.float32)
    
    def train_model(self, X: np.ndarray, y: np.ndarray, validation_split: float = 0.2) -> Dict:
        """Train the product verification model"""
        logger.info("Starting model training...")
        
        # Split data
        X_train, X_val, y_train, y_val = train_test_split(
            X, y, test_size=validation_split, random_state=42, stratify=y
        )
        
        logger.info(f"Training samples: {len(X_train)}, Validation samples: {len(X_val)}")
        
        # Calculate class weights for imbalanced data
        from sklearn.utils.class_weight import compute_class_weight
        class_weights_array = compute_class_weight(
            'balanced', classes=np.unique(y_train), y=y_train
        )
        self.class_weights = {i: class_weights_array[i] for i in range(len(class_weights_array))}
        
        # Create model
        self.model = self.create_advanced_model()
        
        # Callbacks
        callbacks = [
            tf.keras.callbacks.EarlyStopping(
                monitor='val_accuracy',
                patience=10,
                restore_best_weights=True
            ),
            tf.keras.callbacks.ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=5,
                min_lr=1e-7
            ),
            tf.keras.callbacks.ModelCheckpoint(
                'best_product_model.h5',
                monitor='val_accuracy',
                save_best_only=True
            )
        ]
        
        # Data augmentation
        datagen = tf.keras.preprocessing.image.ImageDataGenerator(
            rotation_range=10,
            width_shift_range=0.1,
            height_shift_range=0.1,
            shear_range=0.1,
            zoom_range=0.1,
            horizontal_flip=True,
            fill_mode='nearest'
        )
        
        # Train model
        history = self.model.fit(
            datagen.flow(X_train, y_train, batch_size=32),
            steps_per_epoch=len(X_train) // 32,
            epochs=50,
            validation_data=(X_val, y_val),
            callbacks=callbacks,
            class_weight=self.class_weights,
            verbose=1
        )
        
        self.training_history = history
        
        # Evaluate model
        val_predictions = self.model.predict(X_val)
        val_predictions_binary = (val_predictions > 0.5).astype(int).flatten()
        
        # Generate report
        report = classification_report(y_val, val_predictions_binary, output_dict=True)
        
        training_results = {
            'final_accuracy': float(history.history['val_accuracy'][-1]),
            'final_loss': float(history.history['val_loss'][-1]),
            'classification_report': report,
            'training_samples': len(X_train),
            'validation_samples': len(X_val),
            'epochs_trained': len(history.history['loss']),
            'timestamp': datetime.now().isoformat()
        }
        
        logger.info(f"Training completed! Final validation accuracy: {training_results['final_accuracy']:.4f}")
        
        return training_results
    
    def save_model(self, filepath: str = 'product_verification_model.h5'):
        """Save the trained model"""
        if self.model:
            self.model.save(filepath)
            logger.info(f"Model saved to {filepath}")
            
            # Save training metadata
            metadata = {
                'class_weights': self.class_weights,
                'model_architecture': 'ResNet50-based',
                'input_shape': [224, 224, 3],
                'timestamp': datetime.now().isoformat()
            }
            
            with open(f"{filepath.split('.')[0]}_metadata.json", 'w') as f:
                json.dump(metadata, f, indent=2)
        else:
            logger.error("No model to save")
    
    def load_model(self, filepath: str = 'product_verification_model.h5'):
        """Load a trained model"""
        try:
            self.model = tf.keras.models.load_model(filepath)
            logger.info(f"Model loaded from {filepath}")
            
            # Load metadata if available
            metadata_path = f"{filepath.split('.')[0]}_metadata.json"
            if os.path.exists(metadata_path):
                with open(metadata_path, 'r') as f:
                    metadata = json.load(f)
                    self.class_weights = metadata.get('class_weights')
                    logger.info("Model metadata loaded")
            
        except Exception as e:
            logger.error(f"Error loading model: {e}")
    
    def predict_authenticity(self, image_array: np.ndarray) -> Dict:
        """Predict product authenticity"""
        if self.model is None:
            raise ValueError("Model not loaded")
        
        # Ensure correct input shape
        if len(image_array.shape) == 3:
            image_array = np.expand_dims(image_array, axis=0)
        
        # Make prediction
        prediction = self.model.predict(image_array, verbose=0)
        authenticity_score = float(prediction[0][0])
        
        return {
            'authenticity_score': authenticity_score,
            'is_authentic': authenticity_score > 0.5,
            'confidence': abs(authenticity_score - 0.5) * 2  # Convert to 0-1 confidence
        }
    
    def create_training_report(self) -> str:
        """Generate a comprehensive training report"""
        if not self.training_history:
            return "No training history available"
        
        report = f"""
# Product Verification Model Training Report

## Training Summary
- **Training Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- **Model Architecture**: ResNet50-based CNN
- **Final Validation Accuracy**: {self.training_history.history['val_accuracy'][-1]:.4f}
- **Final Validation Loss**: {self.training_history.history['val_loss'][-1]:.4f}
- **Epochs Trained**: {len(self.training_history.history['loss'])}

## Model Performance
- **Training Accuracy**: {self.training_history.history['accuracy'][-1]:.4f}
- **Training Loss**: {self.training_history.history['loss'][-1]:.4f}
- **Best Validation Accuracy**: {max(self.training_history.history['val_accuracy']):.4f}

## Class Weights
{json.dumps(self.class_weights, indent=2) if self.class_weights else 'Not available'}

## Recommendations
1. Monitor for overfitting if training accuracy >> validation accuracy
2. Consider more diverse training data for better generalization
3. Implement real-world testing with actual product images
4. Regular retraining with new authentic/counterfeit samples
        """
        
        return report

def main():
    """Main training function"""
    # Initialize trainer
    trainer = ProductVerificationTrainer("AIzaSyA427ygmKyS_LOUI2ReR9l6bI5nJ7ARihM")
    
    # Generate training data (in real scenario, use actual product images)
    logger.info("Generating training data...")
    X, y = trainer.generate_synthetic_training_data(num_samples=2000)
    
    # Train model
    results = trainer.train_model(X, y)
    
    # Save model
    trainer.save_model()
    
    # Generate report
    report = trainer.create_training_report()
    with open('training_report.md', 'w') as f:
        f.write(report)
    
    logger.info("Training complete! Check 'training_report.md' for detailed results.")
    
    return results

if __name__ == "__main__":
    main()