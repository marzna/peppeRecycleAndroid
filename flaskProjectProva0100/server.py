from flask import Flask, request, jsonify
import util

app = Flask(__name__)


@app.route('/classify_image', methods=['GET', 'POST'])
def classify_image():
    img_file = "test_images/banana.jpg"

    img = util.get_picture_from_file(img_file)  # dato il path, carica quell'immagine
    img = util.process_picture(img)

    # image_data = request.form["image_data"] #TODO A CHE SERVE?
    #non so se l'immagine sia di dim 224x224
    # response = jsonify(util.label_picture(util.process_picture(image_data), model, labels)) # TODO -> restituisci solo la classe
    response = jsonify(
        util.label_picture(util.process_picture(img), model, labels))  # TODO -> restituisci solo la classe

    response.headers.add('Access-Control-Allow-Origin', '*')

    return response

if __name__ == "__main__":
    print('Avvio server')

    app.run(port=5000)
    print('Server avviato.')

    # img_file = "test_images/banana.jpg"
    #
    # img = util.get_picture_from_file(img_file)  # dato il path, carica quell'immagine
    # img = util.process_picture(img)

    model_file = "garbage.h5"  # path del file contenente il modello
    labels_file = "waste-labels.txt"  # path del file contenente le labels

    model, labels = util.load_artifacts(model_file, labels_file)

