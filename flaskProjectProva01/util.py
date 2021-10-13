# FILE PRESO DOVE_LO_BUTTO_2
import logging

import cv2
import numpy as np
import tensorflow.compat.v1 as tf
from PIL import Image

from keras.models import load_model
from keras.applications.resnet50 import preprocess_input
from keras.preprocessing import image

# --------------------------------------------------
# --------------------------------------------------
# --------------------------------------------------

# Bin - Define labels:
LABEL_CARDBOARD = 'cardboard cartone'
LABEL_GLASS = 'glass vetro'
LABEL_METAL = 'metal metallo'
LABEL_ORGANIC = 'organic organico'
LABEL_PAPER = 'paper carta'
LABEL_PLASTIC = 'plastic plastica'


# LABEL_TRASH = 'trash indifferenziato'

# --------------------------------------------------
# Acquisisco la fotografia da etichettare
# ...
#
# Documentazione qui:
# http://picamera.readthedocs.io/en/release-1.13/recipes1.html
# --------------------------------------------------

def get_picture(file_name):
    # TODO
    """
    # Create a VideoCapture object
    camera = cv2.VideoCapture(0)

    # Set camera resolution
    camera.set(cv2.CAP_PROP_FRAME_WIDTH, 1024)
    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 768)
    ret, frame = camera.read()
    cv2.imwrite("camera-images/waste.jpg", frame)
    """
    input_height = 224
    input_width = 224
    img = image.load_img(file_name, target_size=(input_height, input_width))
    img = image.img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
    """
    img = image.load_img(file_name, target_size=(input_height, input_width))
    # Open image using Image module
    img = Image.open("test_images/banana.jpg")
    # Show actual Image
    img.show()
    img = image.img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
    """
    return img

def load_graph(model_file):
    graph = tf.Graph()
    graph_def = tf.GraphDef()

    with open(model_file, "rb") as f:
        graph_def.ParseFromString(f.read())
    with graph.as_default():
        tf.import_graph_def(graph_def)

    return graph


def read_tensor_from_image_file(file_name, input_height=299, input_width=299, input_mean=0, input_std=255):
    input_name = "file_reader"
    output_name = "normalized"
    file_reader = tf.read_file(file_name, input_name)
    if file_name.endswith(".png"):
        image_reader = tf.image.decode_png(file_reader, channels=3, name="png_reader")
    elif file_name.endswith(".gif"):
        image_reader = tf.squeeze(tf.image.decode_gif(file_reader, name="gif_reader"))
    elif file_name.endswith(".bmp"):
        image_reader = tf.image.decode_bmp(file_reader, name="bmp_reader")
    else:
        image_reader = tf.image.decode_jpeg(file_reader, channels=3, name="jpeg_reader")

    float_caster = tf.cast(image_reader, tf.float32)
    dims_expander = tf.expand_dims(float_caster, 0)
    resized = tf.image.resize_bilinear(dims_expander, [input_height, input_width])
    normalized = tf.divide(tf.subtract(resized, [input_mean]), [input_std])
    sess = tf.Session()
    result = sess.run(normalized)

    return result


def load_labels(label_file):
    label = []
    proto_as_ascii_lines = tf.gfile.GFile(label_file).readlines()
    for l in proto_as_ascii_lines:
        label.append(l.rstrip())

    return label


def load_net():
    model_file = "waste-model/garbage.h5"
    graph = load_model(model_file)
    print("modello caricato")
    return graph


def label_picture():
    logging.debug('Labelling picture...')

    # --------------------------------------------------
    # file_name = "tensorflow/examples/label_image/data/grace_hopper.jpg"
    # model_file = "tensorflow/examples/label_image/data/inception_v3_2016_08_28_frozen.pb"
    # label_file = "tensorflow/examples/label_image/data/imagenet_slim_labels.txt"
    # input_height = 299
    # input_width = 299
    # input_mean = 0
    # input_std = 255
    # input_layer = "input"
    # output_layer = "InceptionV3/Predictions/Reshape_1"
    # --------------------------------------------------
    file_name = "test_images/test.jpg"

    label_file = "waste-labels.txt"
    """
    input_height = 224
    input_width = 224
    """

    # t = read_tensor_from_image_file(file_name, input_height=input_height, input_width=input_width, input_mean=input_mean, input_std=input_std)
    img = get_picture(file_name)
    """
    img = image.load_img(file_name, target_size=(input_height, input_width))
    img = image.img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
    """

    model_file = "garbage.h5"
    try:
        graph = load_model(model_file)
        print("Modello caricato")
        # graph.summary()
        results = graph.predict(img) # sarà un array contenente il valore della confidenza per ogni classe di rifiuto.
    except:
        print("Errore")

    # print("Risultati:", results)

    predicted_class_index = np.argmax(results, axis=1)[0] # Viene scelta la classe con maggior confidenza.
    # Questo valore conterrà l'indice della classe. ES: indice=3 => organico
    # print("\n\nClassificazione effettuata! Indice label:", predicted_class_index)

    labels = load_labels(label_file)
    # print("\n\nLabels:", labels)

    return labels[predicted_class_index], results[0, predicted_class_index] #label con migliore confidenza + valore relativa confidenza

    logging.debug('Labelled picture!')


# --------------------------------------------------
# ...
# --------------------------------------------------

if __name__ == '__main__':
    """ Configurazione del logging """
    logging.basicConfig(level=logging.DEBUG, format='[%(levelname)s] [%(threadName)-10s] >>> %(message)s', )

    logging.debug('...')

    # Acquisisco la fotografia
    #get_picture()

    # Classifico la fotografia
    label, confidence = label_picture()
    print("\nQuesto rifiuto andrà smaltito nella raccolta", label,
          "con una confidenza pari a", confidence)
