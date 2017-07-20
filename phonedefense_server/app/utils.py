import os

from flask import current_app


def delete_file_folder(src):
    if os.path.isfile(src):
        os.remove(src)
    elif os.path.isdir(src):
        for item in os.listdir(src):
            itemsrc=os.path.join(src,item)
            delete_file_folder(itemsrc)
            os.rmdir(src)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1] in current_app.config['ALLOWED_EXTENSIONS']
