#coding: utf-8
import sys
import os
import copy
import cv2
import dlib
import numpy as np
from operator import itemgetter

def get_parts_pos(image):
  try:
    #変数定義
    ret_str = ""
    PREDICTOR_PATH = '/var/www/html/nty_img/shape_predictor_68_face_landmarks.dat'
    detector = dlib.get_frontal_face_detector()
    predictor = dlib.shape_predictor(PREDICTOR_PATH)
    rects = detector(image, 1)
    if len(rects) != 0:
      for rect in rects:
        landmarks = predictor(image, rect).parts()
        #目の位置
        eye_pos = itemgetter(39,42)(landmarks)
        #目尻の位置
        eye_end_pos = itemgetter(36,45)(landmarks)
        #目と目の距離算出
        p1 = np.array([eye_pos[0].x, eye_pos[0].y])
        p2 = np.array([eye_pos[1].x, eye_pos[1].y])
        eye_distance = np.linalg.norm(p1-p2)
        ret_str += str(round(eye_distance,1))+"/"
        #輪郭の位置
        outline_pos = landmarks[0:16]
        outline_start_end_pos = itemgetter(0,16)(landmarks)
        #顔が斜めか
        #輪郭と目尻の距離を算出
        eye_end_right_p = np.array([eye_end_pos[0].x, eye_end_pos[0].y])
        eye_end_left_p = np.array([eye_end_pos[1].x, eye_end_pos[1].y])
        outline_start_end_right_p = np.array([outline_start_end_pos[0].x, outline_start_end_pos[0].y])
        outline_start_end_left_p = np.array([outline_start_end_pos[1].x, outline_start_end_pos[1].y])
        eye_outline_distance_right = np.linalg.norm(eye_end_right_p-outline_start_end_right_p)
        eye_outline_distance_left = np.linalg.norm(eye_end_left_p-outline_start_end_left_p)
        #斜めの場合少し調整する
        if (abs(eye_outline_distance_left-eye_outline_distance_right)<20):
          looking_straight = True
          #print("* Looking straight")
        else:
          looking_straight = False
          #print("* Looking left/Right")
        #輪郭と下唇の距離
        p1 = np.array([landmarks[8].x, landmarks[8].y])
        p2 = np.array([landmarks[57].x, landmarks[57].y])
        mouth_outline_distance = np.linalg.norm(p1-p2)
        #ななめむいてたらそのぶん補正
        if (looking_straight):
          minus_n = 0
        else:
          minus_n = 20
        mouth_outline_distance = mouth_outline_distance-minus_n
        ret_str += str(round(mouth_outline_distance,1))+"/"
        #鼻の位置
        nose_pos = landmarks[27:36]
        #鼻の幅
        nose_end_pos = itemgetter(48,54)(landmarks)
        p1 = np.array([nose_end_pos[0].x, nose_end_pos[0].y])
        p2 = np.array([nose_end_pos[1].x, nose_end_pos[1].y])
        nose_wid = np.linalg.norm(p1-p2)
        if (looking_straight):
          plus_n = 0
        else:
          plus_n = 20
        ret_str += str(round(nose_wid+plus_n,1))+"/"
        #鼻を切り出す
        under_left = (landmarks[31].x, landmarks[33].y)
        over_right = (landmarks[35].x, landmarks[27].y)
        #trim_nose_img = image[over_right[1]-5:under_left[1], under_left[0]-20:over_right[0]+15]
        trim_nose_img_center = image[over_right[1]+100:under_left[1]-30, under_left[0]:over_right[0]]
        #鼻の中心部の平均色を取得
        b = trim_nose_img_center.T[0].flatten().mean()
        g = trim_nose_img_center.T[1].flatten().mean()
        r = trim_nose_img_center.T[2].flatten().mean()
        ret_str += (str(round(r))+","+str(round(g))+","+str(round(b)))+"/"
        #口の位置
        mouth_pos = landmarks[48:68]
        #口開閉
        #上唇
        mouth_pos_u = np.array([landmarks[62].x, landmarks[62].y])
        #下唇
        mouth_pos_s = np.array([landmarks[66].x, landmarks[66].y])
        mouth_open_distance = np.linalg.norm(mouth_pos_u-mouth_pos_s)
        #口の中点を取得
        mouth_pos_center = (int((landmarks[48].x+landmarks[54].x)/2),int((landmarks[48].y+landmarks[54].y)/2))
        #口開閉描画
        mouth_open_pos = itemgetter(62,66)(landmarks)
        #鼻と口の距離
        nose_pos_p = np.array([landmarks[33].x, landmarks[33].y])
        mouth_pos_center_nparr = np.array([mouth_pos_center[0], mouth_pos_center[1]])
        nose_mouth_distance = np.linalg.norm(nose_pos_p-mouth_pos_center_nparr)
        #口が一定値以上開いているとき口角が上がる調整
        if (mouth_open_distance>25):
          nose_mouth_distance = nose_mouth_distance+20
        ret_str += str(round(nose_mouth_distance,1))
        if ret_str == None:
          return "error/None"
        else:
          return ret_str
        return "error/face not found"
    else:
      return "error/face not found"
  except:
    return "error/--"

def get_degree_of_similarity(res1, res2):
  res1_arr = res1.split("/")
  res2_arr = res2.split("/")
  if (res1_arr[0] == "error" or res2_arr[0] == "error"):
    return "error"
  a0_dif = round(abs(float(res1_arr[0])-float(res2_arr[0])),2)
  a1_dif = round(abs(float(res1_arr[1])-float(res2_arr[1])),2)
  a2_dif = round(abs(float(res1_arr[2])-float(res2_arr[2])),2)
  #color
  res1_color_arr = res1_arr[3].split(",")
  res2_color_arr = res2_arr[3].split(",")
  a3_r_dif = round(abs(float(res1_color_arr[0])-float(res2_color_arr[0])),2)
  a3_g_dif = round(abs(float(res1_color_arr[1])-float(res2_color_arr[1])),2)
  a3_b_dif = round(abs(float(res1_color_arr[2])-float(res2_color_arr[2])),2)
  a4_dif = round(abs(float(res1_arr[4])-float(res2_arr[4])),2)
  if ((15>=a0_dif)):
    #各項目ごとにスコア加算
    score = 0
    #輪郭とくちびる
    if ((15>=a1_dif)):
      score += 2
    elif ((10>=a1_dif)):
      score += 1
    else:
      score += 0
    #鼻の幅
    if ((15>=a2_dif)):
      score += 2
    elif ((10>=a2_dif)):
      score += 1
    else:
      score += 0
    #鼻と口の距離
    if ((15>=a4_dif)):
      score += 2
    elif ((10>=a4_dif)):
      score += 1
    else:
      score += 0
    #色
    score_c = 0;
    if ((20>=a3_r_dif) and (20>=a3_g_dif) and (20>=a3_b_dif)):
      score_c += 1
    else:
      score_c += 0
    #確率算出
    if (score > 4):
      score = (score+4)*10
    if (score == 4):
      score = (score+score_c)*10
    elif (score<=3):
      score = score*10
    return score
  else:
    return 0
