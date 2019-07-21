#!/bin/bash
#
# ---------------------------------------------------------------------
# Tcp tester startup script.
# ---------------------------------------------------------------------
#

message()
{
  TITLE="Cannot start TCP tester"
  if [ -n "`which zenity`" ]; then
    zenity --error --title="$TITLE" --text="$1"
  elif [ -n "`which kdialog`" ]; then
    kdialog --error "$1" --title "$TITLE"
  elif [ -n "`which xmessage`" ]; then
    xmessage -center "ERROR: $TITLE: $1"
  elif [ -n "`which notify-send`" ]; then
    notify-send "ERROR: $TITLE: $1"
  else
    printf "ERROR: $TITLE\n$1\n"
  fi
}

JAVA=`which java`
READLINK=`which readlink`

SCRIPT_LOCATION=$0
if [ -x "$READLINK" ]; then
  while [ -L "$SCRIPT_LOCATION" ]; do
    SCRIPT_LOCATION=`"$READLINK" -e "$SCRIPT_LOCATION"`
  done
fi

SCRIPT_DIRECTORY=`dirname "$SCRIPT_LOCATION"`

case $1 in
  -h|--help)
    echo "USAGE: `basename $SCRIPT_LOCATION` [binary-dir] [binary-name]"
    exit 1
    ;;
esac


# ---------------------------------------------------------------------
# Locate a JDK installation directory which will be used to run the IDE.
# Try (in order): JDK_HOME, JAVA_HOME, "java" in PATH.
# ---------------------------------------------------------------------
if [ -z "$JDK" -a -n "$JDK_HOME" -a -x "$JDK_HOME/bin/java" ]; then
  JDK="$JDK_HOME"
fi

if [ -z "$JDK" -a  -n "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ]; then
  JDK="$JAVA_HOME"
fi

if [ -z "$JDK" ]; then
  JDK_PATH=`which java`

  if [ -n "$JDK_PATH" ]; then
    if [ "$OS_TYPE" = "FreeBSD" -o "$OS_TYPE" = "MidnightBSD" ]; then
      JAVA_LOCATION=`JAVAVM_DRYRUN=yes java | "$GREP" '^JAVA_HOME' | "$CUT" -c11-`
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    elif [ "$OS_TYPE" = "SunOS" ]; then
      JAVA_LOCATION="/usr/jdk/latest"
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    elif [ "$OS_TYPE" = "Darwin" ]; then
      JAVA_LOCATION=`/usr/libexec/java_home`
      if [ -x "$JAVA_LOCATION/bin/java" ]; then
        JDK="$JAVA_LOCATION"
      fi
    fi
  fi

  if [ -z "$JDK" -a -x "$READLINK" -a -x "$XARGS" -a -x "$DIRNAME" ]; then
    JAVA_LOCATION=`"$READLINK" -f "$JDK_PATH"`
    case "$JAVA_LOCATION" in
      */jre/bin/java)
        JAVA_LOCATION=`echo "$JAVA_LOCATION" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME"`
        if [ ! -d "$JAVA_LOCATION/bin" ]; then
          JAVA_LOCATION="$JAVA_LOCATION/jre"
        fi
        ;;
      *)
        JAVA_LOCATION=`echo "$JAVA_LOCATION" | "$XARGS" "$DIRNAME" | "$XARGS" "$DIRNAME"`
        ;;
    esac
    if [ -x "$JAVA_LOCATION/bin/java" ]; then
      JDK="$JAVA_LOCATION"
    fi
  fi
fi

JAVA_BIN="$JDK/bin/java"
if [ -z "$JDK" -o ! -x "$JAVA_BIN" ]; then
  message "No JDK found. Please validate either STUDIO_JDK, JDK_HOME or JAVA_HOME environment variable points to valid JDK installation."
  exit 1
fi


# ---------------------------------------------------------------------
# Locate if JDK has JavaFX.
# It could be installed else where, you can try comment this and try it.
# ---------------------------------------------------------------------
if [ ! -f "$JDK/jre/lib/javafx.properties" ]; then
  message "You probably not have openjfx installed"
  exit 1
fi

BIN_DIR=${1:-"$SCRIPT_DIRECTORY/../dist"}
BIN_NAME=${2:-"tcp_tester.jar"}

BINARY="$BIN_DIR/$BIN_NAME"

if [ ! -f "$BINARY" ]; then
  message "Could not find jar file"
  exit 2
fi

"$JAVA_BIN" -jar "$BINARY" "$@"