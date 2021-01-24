import os
from flask import Flask, request, redirect, url_for, jsonify
from werkzeug import secure_filename



app = Flask(__name__)

@app.route('/', methods=['GET'])
def cc():
    return jsonify(status=200,value="x")

@app.route('/get_result', methods=['POST'])
def classify():
    if request.method == 'POST':
        file = request.files['file']
        data=[0,1]
        if file:
            filename = secure_filename(file.filename)
            file.save(os.path.join("/tmp/", filename))
            


            return jsonify(status=200,value=1)
    return jsonify(status=500)



if __name__ == '__main__':
    # app.debug = True
    app.run()