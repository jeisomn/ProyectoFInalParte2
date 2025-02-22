# Report on the Classification of Traffic Signs Using SIFT and LBP

## Salesian Polytechnic University  
### Computer Vision  

**Members:**  
- Pañora Jeison  
- Sigua Paúl  

**Teacher:**  
- Vladimir Robles  

**Repository:** [Proyecto Final Parte 2](https://github.com/jeisomn/ProyectoFInalParte2)  
**Video:** [YouTube](https://youtu.be/b_E5R5uqbLQ)  

---

## 1. Introduction

The objective of this project is to develop a system capable of classifying two types of traffic signs: **"speed bump"** and **"traffic light"**, using feature extraction techniques, specifically **SIFT** (Scale-Invariant Feature Transform) and **LBP** (Local Binary Patterns), implemented with OpenCV.

This work is framed by the need to equip driver assistance systems or autonomous vehicles with robust methods for identifying and processing traffic signs in real-world environments, where lighting conditions, viewing angles, and scales may vary considerably.

---

## 2. Problem Description

The central problem is the **accurate recognition and classification of traffic signs** in images, given the following challenges:

- **Variations in lighting and environmental conditions:** Images may exhibit changes in brightness, contrast, and shadows.  
- **Scale and orientation changes:** Traffic signs can appear in different sizes and rotations.  
- **Background diversity and noise:** The presence of distracting visual elements complicates the precise segmentation of the sign.  

These challenges require a solution that combines robust techniques for detecting invariant features (**SIFT**) with efficient methods for texture extraction (**LBP**).

---

## 3. Proposed Solution

### 3.1 System Architecture

The system is divided into modules that integrate to achieve the final objective. The repository is organized into three main directories:

- **`Sift/`**: Implements and tests the SIFT algorithm to detect and describe keypoints in images, providing robustness against changes in scale and rotation.
- **`app/`**: Contains the application that enables user interaction, integrating image processing and real-time or batch classification of traffic signs.
- **`final/`**: Contains the final integrated version of the system, where feature extraction modules and the user interface are combined, along with classification algorithms utilizing the generated descriptors.

### 3.2 Schematic Diagram of the Solution

![Solution Diagram](path/to/your/image.png)  

### 3.3 Details of the Techniques Used

- **SIFT**: Based on the work by Lowe (2004), this algorithm identifies scale- and rotation-invariant keypoints, generating robust descriptors for each key region in the image.
- **LBP**: Inspired by the proposal of Ahonen et al. (2006), LBP extracts local texture patterns by comparing neighboring pixels, constructing histograms that summarize the texture information of the image.

---

## 4. Tools

### 4.1 OpenCV

Used for reading, processing, and manipulating images, as well as for the implementation of SIFT and related operations.

### 4.2 Python

Programming language used for project development, given the popularity of libraries such as **OpenCV** and **scikit-image** for computer vision tasks.

---

## 5. Conclusions

The system developed in the repository proves to be a robust solution for traffic sign classification by integrating feature extraction techniques like **SIFT** and **LBP**.

### Key Findings:

- **Effectiveness in Classification:**
  - The combination of **SIFT** and **LBP** achieves high levels of precision, sensitivity, and specificity, confirming the viability of the proposed solution.
- This project lays the foundation for future research and improvements, such as:
  - Incorporating **deep learning techniques** for feature extraction.
  - Optimizing real-time processing.

---

## 6. Bibliography

1. [OpenCV Documentation](https://docs.opencv.org/)  
2. [Scikit-Image Documentation](https://scikit-image.org/docs/stable/)  
3. [Introduction to SIFT - Medium](https://medium.com/@deepanshut041/introduction-to-sift-scale-invariant-feature-transform-65d7f3a72d40)  
4. [Understanding the Local Binary Pattern (LBP) - Medium](https://aihalapathirana.medium.com/understanding-the-local-binary-pattern-lbp-a-powerful-method-for-texture-analysis-in-computer-4fb55b3ed8b8)  
5. [OpenCV: Introduction to SIFT](https://docs.opencv.org/4.x/da/df5/tutorial_py_sift_intro.html)  
6. [Local Binary Patterns with Python & OpenCV - PyImageSearch](https://pyimagesearch.com/2015/12/07/local-binary-patterns-with-python-opencv/)  
