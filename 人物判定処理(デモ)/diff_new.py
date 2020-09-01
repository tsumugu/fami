#coding: utf-8
import os
import sys
import glob
import time
import json
import cv2
sys.path.append('/var/www/html/nty_img/')
import get_parts_pos as gp

args = sys.argv
image_path = args[1]
file_name = (image_path.split("/")[7]).replace(".jpeg", "")
family_id = file_name.split("-")[0]
return_json_dic  = {}

#計算済みscoreファイルを読み込んで配列を作成
pp_family_members = {}
for f in glob.glob("/var/www/html/nty_img/score/"+family_id+"-*.txt"):
  name_spl = f.split("/")
  name_spl = name_spl[6].split("-")
  name = name_spl[1].replace(".txt","")
  f = open(f)
  read_score = f.read()
  f.close()
  rl = read_score.split("/")
  if rl[0] != "error":
    pp_family_members[name] = read_score
#print(pp_family_members)

#渡された画像からscore算出
face_margin = 10;
face_image_size = 500
if os.path.exists(image_path) and (len(pp_family_members) != 0):
  image = cv2.imread(image_path, cv2.IMREAD_COLOR)
  faceCascade = cv2.CascadeClassifier('/var/www/html/haarcascade_frontalface_default.xml')
  image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
  face = faceCascade.detectMultiScale(image_gray, 1.11, 3)
  if len(face) > 0:
    return_json_dic["status"] = "OK"
    family_score_dic = []
    # 顔を取り出して処理していく
    for rect in face:
      family_score_dic_tmp = []
      trim_face_img = image[rect[1]:rect[1]+rect[3]+face_margin,rect[0]:rect[0]+rect[2]+face_margin]
      trim_face_img = cv2.resize(trim_face_img, (face_image_size, face_image_size))
      # スコアを算出
      ret_pp = gp.get_parts_pos(trim_face_img)
      count = 0
      for pp_family_key in pp_family_members:
        # 計算済みスコアと今回のスコアを比較
        sim = gp.get_degree_of_similarity(ret_pp, pp_family_members[pp_family_key])
        if type(sim) is int:
          if sim>=40:
            # 閾値以上(つまり、同一人物だったら、配列にぶち込む)
            family_score_dic_tmp.append([pp_family_key,"",sim])
            count += 1
        else:
          count += 1
      #一致する人なし→不審者？
      if count == 0:
        #画像保存
        file_path = '/var/www/html/nty_img/diff/unknown/'+family_id+'-'+str(round(time.time()))+'.jpg'
        cv2.imwrite(file_path, trim_face_img)
        #txtファイル作成
        text_path = '/var/www/html/nty_img/diff/unknown_text/'+family_id+'-'+str(round(time.time()))+'.txt'
        file = open(text_path, 'w')
        file.write(str(round(time.time()))+","+file_path)
        file.close()
        family_score_dic.append(["unknown",file_path,0])
      else:
        #類似度順に並び替え
        if len(family_score_dic_tmp) > 0:
          family_score_dic_tmp_sorted = sorted(family_score_dic_tmp, key=lambda x:x[2], reverse=True)
          family_score_dic.append(family_score_dic_tmp_sorted[0])
    return_json_dic["status"] = "OK"
    return_json_dic["users_face_list"] = family_score_dic
  else:
    return_json_dic["status"] = "NG"
else:
  return_json_dic["status"] = "NG"
# jsonで返す
print(json.dumps(return_json_dic))
