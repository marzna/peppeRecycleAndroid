from flask import Flask, request
from PIL import Image
import util

app = Flask(__name__)

model_file = "garbage.h5"  # path del file contenente il modello
labels_file = "waste-labels-pepper-android.txt"  # path del file contenente le labels

model, labels = util.load_artifacts(model_file, labels_file)


@app.route('/')
def home():
    return "hello"


# Classificazione
@app.route('/handle_request', methods=['GET', 'POST'])
def handle_request():
    img = request.files['image']
    img = Image.open(img.stream)
    img = util.process_picture(img)
    label = util.label_picture(img, model, labels)  # Classificazione
    print("Questo rifiuto va smistato nella raccolta", label)

    return str(label)


if __name__ == "__main__":
    print('Avvio server')

    app.run(host="127.0.0.1", port=5000, debug=False)

    print('Server avviato.')
