import logging
import numpy as np
import tensorflow.compat.v1 as tf

from tensorflow.keras.models import load_model
from keras.applications.resnet50 import preprocess_input
from keras.preprocessing import image

# Bidoni - Definizione labels
LABEL_CARDBOARD = 'cardboard'
LABEL_GLASS = 'glass'
LABEL_METAL = 'metal'
LABEL_ORGANIC = 'organic'
LABEL_PAPER = 'paper'
LABEL_PLASTIC = 'plastic'


def get_picture_from_file(file_name):
    input_height = 224
    input_width = 224
    img = image.load_img(file_name, target_size=(input_height, input_width))
    return img


def process_picture(img):
    img = img.resize((224, 224))
    img = image.img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
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


# Funzione di classificazione
def label_picture(img, graph, labels):
    logging.debug('Labelling picture...')

    results = graph.predict(img)  # sarà un array contenente il valore della confidenza per ogni classe di rifiuto.

    predicted_class_index = np.argmax(results, axis=1)[0]  # Viene scelta la classe con maggior confidenza.
    # Questo valore conterrà l'indice della classe. ES: indice=3 => organico
    logging.debug('Labelled picture!')

    return labels[predicted_class_index]  # label con migliore confidenza


# stesso utilizzo di label_picture. Tuttavia, viene restituita anche la confidenza
def label_picture_with_confidence(img, graph, labels):
    logging.debug('Labelling picture...')

    results = graph.predict(img)  # sarà un array contenente il valore della confidenza per ogni classe di rifiuto.

    predicted_class_index = np.argmax(results, axis=1)[0]  # Viene scelta la classe con maggior confidenza.
    # Questo valore conterrà l'indice della classe. ES: indice=3 => organico
    logging.debug('Labelled picture!')

    return labels[predicted_class_index], results[
        0, predicted_class_index]  # label con migliore confidenza + valore relativa confidenza


def load_artifacts(model_file, label_file):
    return load_model(model_file), load_labels(label_file)  # restituisce modello e labels


if __name__ == '__main__':
    """ Configurazione del logging """
    logging.basicConfig(level=logging.DEBUG, format='[%(levelname)s] [%(threadName)-10s] >>> %(message)s', )
    logging.debug('...')
