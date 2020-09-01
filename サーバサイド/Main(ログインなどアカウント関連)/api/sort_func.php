<?php
function sortByKey($key_name, $sort_order, $array) {
  if (empty($array)) {
    return $array;
  }
  foreach ($array as $key => $value) {
    $standard_key_array[$key] = $value[$key_name];
  }
  array_multisort($standard_key_array, $sort_order, $array);
  return $array;
}
