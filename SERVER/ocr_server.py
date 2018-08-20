import pytesseract
import socket
import sys
from PIL import Image
from http.server import BaseHTTPRequestHandler,HTTPServer
from os import curdir, sep
import cgi
import sys
from pprint import pprint
import urllib.parse
import codecs
import random
import os
import json
from socketserver import ThreadingMixIn
import threading
import sqlite3 as sql
import json
import operator
from collections import OrderedDict
import numpy as np
import face_recognition as fr
from PIL import Image
from random import random
import io

PORT_NUMBER = 24000

#This class will handles any incoming request from
#the browser 


class myHandler(BaseHTTPRequestHandler):
    
    """
    Custom functions to process incoming and outgoing signals
    
    """
    def extract_text(self,im):
        mplr = 10
        new_size = (im.size[0] * mplr ,im.size[1] * mplr)
        im = im.resize(new_size, Image.ANTIALIAS)
        text = pytesseract.image_to_string(im)
        print('Text Detected!')
        return text

    """
    End of Custom
    
    """
    
    #Handler for the GET requests
    def do_GET(self):
        print('do GET')
        return

    #Handler for the POST requests
    def do_POST(self):
        pprint (vars(self))
        length = int(self.headers['Content-Length'])
   
        
        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={'REQUEST_METHOD':'POST'}
        )
        
       
        image = form['fileUpload'].value
        image = Image.open(io.BytesIO(image))
        response = self.extract_text(image)
        
        
        # Send back response after the data is processed
        
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Content-type','text/html')
        
        self.end_headers()
        # Send the html message
        self.wfile.write(response.encode())
        

        return
    
    def do_OPTIONS(self):
        self.send_response(200, "ok")
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, OPTIONS')
        self.send_header("Access-Control-Allow-Headers", "X-Requested-With")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()
        
        return

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""

try:
    #Create a web server and define the handler to manage the
    #incoming request
    server = ThreadedHTTPServer(('', PORT_NUMBER), myHandler)
    print('Started httpserver on port ' , PORT_NUMBER)
    
    #Wait forever for incoming htto requxests
    server.serve_forever()

except KeyboardInterrupt:
    print('^C received, shutting down the web server')
    server.socket.close()