#coding: utf-8
import sys
import os
import json
import cv2
import get_parts_pos as gp

#画像から顔を取得
image_path = sys.argv[1]
return_json_dic = {}
#print(image_path)
if os.path.exists(image_path):
  image = cv2.imread(image_path, cv2.IMREAD_COLOR)
  ret_pp = gp.get_parts_pos(image)
  #print(ret_pp)
  return_json_dic["status"] = "OK"
  return_json_dic["score"] = ret_pp
else:
  return_json_dic["status"] = "NG"
print(json.dumps(return_json_dic))
