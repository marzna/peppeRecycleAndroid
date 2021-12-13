import requests

# File utile per testare la richiesta al server.
stringa = "http://127.0.0.1:5000/handle_request"
files = {'image': open('test_images/test.jpg', 'rb')}
requests.post(stringa, files=files)
