name=$1
# restart start stop
action=$2

is_exist(){
  pid=`ps -ef | grep $name | grep -v grep | awk '{print $1}'`
  if [$pid -eq '']
  then
    return 0
  else
    return 1
  fi
}


start(){
  status=is_exist
  if [$status -eq '0']
  then
    echo '没有启动'
  else
    echo '执行中'
  fi
}

case $action in
  'start')
  start
  ;;
