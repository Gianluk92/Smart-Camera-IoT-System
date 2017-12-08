all: door-windowSensor.c

SMALL=1

CONTIKI=/home/user/contiki-3.0
CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"


APPS += er-coap
APPS += rest-engine

CONTIKI_WITH_IPV6 = 1
include $(CONTIKI)/Makefile.include
