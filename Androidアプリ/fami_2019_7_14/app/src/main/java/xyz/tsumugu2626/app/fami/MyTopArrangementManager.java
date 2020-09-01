package xyz.tsumugu2626.app.fami;

public class MyTopArrangementManager {
    public static int[] get_top_arrangement(int size) {
        if (size<1 || size>8) {
            return null;
        }
        int[] pos_1 = {0,1,0, 0,9,0, 0,0,0};
        int[] pos_2 = {0,0,0, 1,9,1, 0,0,0};
        int[] pos_3 = {0,1,0, 1,9,1, 0,0,0};
        int[] pos_4 = {0,1,0, 1,9,1, 0,1,0};
        int[] pos_5 = {1,1,0, 1,9,1, 0,1,0};
        int[] pos_6 = {1,1,1, 1,9,1, 0,1,0};
        int[] pos_7 = {1,1,1, 1,9,1, 1,1,0};
        int[] pos_8 = {1,1,1, 1,9,1, 1,1,1};
        int[][] arr = {pos_1, pos_2, pos_3, pos_4, pos_5, pos_6, pos_7, pos_8};
        size = size-1;
        if (arr[size] != null) {
            return arr[size];
        } else {
            return null;
        }
    }
}
