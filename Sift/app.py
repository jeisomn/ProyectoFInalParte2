from flask import Flask, request, jsonify
import cv2
import numpy as np
import os
from io import BytesIO
from PIL import Image
import tempfile

app = Flask(__name__)

# Inicializar el detector SIFT
sift = cv2.SIFT_create()

# Cargar las imágenes de logos desde directorios
logo1_dir = "/home/jeison/Escritorio/ProyectoFinal/Sift/static/logo1/"
logo2_dir = "/home/jeison/Escritorio/ProyectoFinal/Sift/static/logo2/"

# Verificar que los directorios existen
if not os.path.exists(logo1_dir) or not os.path.exists(logo2_dir):
    print("Error: Los directorios no existen.")
    exit()

# Función para cargar las imágenes de logos y calcular los descriptores SIFT
def cargar_logo_imagenes(logo_dir):
    logos = []
    descriptores = []
    puntos_clave = []
    for filename in os.listdir(logo_dir):
        if filename.endswith(".png") or filename.endswith(".jpg"):
            logo = cv2.imread(os.path.join(logo_dir, filename))
            if logo is not None:
                kp, des = sift.detectAndCompute(logo, None)
                if des is not None:
                    logos.append(logo)
                    descriptores.append(des)
                    puntos_clave.append(kp)
    return logos, puntos_clave, descriptores

# Cargar los logos y sus descriptores y puntos clave
logo1_imagenes, kp_logo1, des_logo1 = cargar_logo_imagenes(logo1_dir)
logo2_imagenes, kp_logo2, des_logo2 = cargar_logo_imagenes(logo2_dir)

# Inicializar el detector BFMatcher
bf = cv2.BFMatcher(cv2.NORM_L2, crossCheck=False)

# Función para filtrar coincidencias usando el ratio de Lowe
def filtrar_coincidencias(matches, ratio):
    matches_filtrados = []
    for match_pair in matches:
        if match_pair[0].distance < ratio * match_pair[1].distance:
            matches_filtrados.append(match_pair[0])
    return matches_filtrados

# Función para procesar la imagen recibida desde la app
def procesar_imagen(imagen_bytes):
    # Convertir los bytes a una imagen OpenCV
    image = Image.open(BytesIO(imagen_bytes))
    image = np.array(image)

    # Detectar y calcular los puntos clave y descriptores para el frame
    kp_frame, des_frame = sift.detectAndCompute(image, None)

    img_matches = image.copy()

    # Umbral para las coincidencias
    ratio = 0.5

    # Buscar coincidencias con las imágenes de logo1
    if des_frame is not None and len(des_logo1) > 0:
        for i, des_logo in enumerate(des_logo1):
            matches1 = bf.match(des_logo, des_frame)
            matches1_pairs = [(matches1[j], matches1[j + 1]) for j in range(len(matches1) - 1)]
            matches1_filtered = filtrar_coincidencias(matches1_pairs, ratio)
            if len(matches1_filtered) > 15:
                img_matches = cv2.drawMatches(logo1_imagenes[i], kp_logo1[i], image, kp_frame, matches1_filtered, None)

    # Buscar coincidencias con las imágenes de logo2
    if des_frame is not None and len(des_logo2) > 0:
        for i, des_logo in enumerate(des_logo2):
            matches2 = bf.match(des_logo, des_frame)
            matches2_pairs = [(matches2[j], matches2[j + 1]) for j in range(len(matches2) - 1)]
            matches2_filtered = filtrar_coincidencias(matches2_pairs, ratio)
            if len(matches2_filtered) > 15:
                img_matches = cv2.drawMatches(logo2_imagenes[i], kp_logo2[i], image, kp_frame, matches2_filtered, None)

    # Convertir la imagen procesada a formato JPEG
    _, jpeg = cv2.imencode('.jpg', img_matches)
    return jpeg.tobytes()

@app.route('/upload', methods=['POST'])
def upload_image():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    # Leer la imagen desde el archivo recibido
    imagen_bytes = file.read()

    # Procesar la imagen
    processed_image = procesar_imagen(imagen_bytes)

    # Retornar la imagen procesada
    return (processed_image, 200, {'Content-Type': 'image/jpeg'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
