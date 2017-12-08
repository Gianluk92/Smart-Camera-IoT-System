#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include <stdio.h>

static int light_value = 0;
static int room_number = 0;

void periodic_handler_value();
void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void get_periodic_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void post_periodic_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


RESOURCE(setting, "title=\"setting\";type=\"LIGHT\";rt=\"room_number\"", get_handler, post_handler, NULL, NULL);
PERIODIC_RESOURCE(light,"title=\"light\";obs;rt=\"light_value\";if=\"1\"",get_periodic_handler,post_periodic_handler,
					NULL,NULL,50*CLOCK_SECOND,periodic_handler_value);

void get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[30];
	int length = 30;
	
	sprintf(message, "%u",room_number);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

void post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len = 10, new_value;
	const char *val = NULL;

	len = REST.get_post_variable(request,"room",&val);
	
	if( len > 0 ){
    	new_value = atoi(val);	
    	room_number = new_value;
    	REST.set_response_status(response, REST.status.CREATED);
    } 
    else 
    	REST.set_response_status(response, REST.status.BAD_REQUEST);
}


void get_periodic_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	/* Populat the buffer with the response payload*/
	char message[10];
	int length = 10;
	
	sprintf(message, "%u", light_value);
	length = strlen(message);
	memcpy(buffer, message, length);

	REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}

// set initial light-value
void post_periodic_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len = 30, new_value;
	const char *val = NULL;

	len = REST.get_post_variable(request,"light_set",&val);
	
	if( len > 0 ){
    	new_value = atoi(val);	
    	light_value = new_value;
    	REST.set_response_status(response, REST.status.CREATED);
    } 
    else 
    	REST.set_response_status(response, REST.status.BAD_REQUEST);
  	
}

// Simulate the variation of the light during a day
void periodic_handler_value()
{
	light_value = (light_value+10)%900;
    REST.notify_subscribers(&light);
}

PROCESS(server, "CoAP Server");
AUTOSTART_PROCESSES(&server);
PROCESS_THREAD(server, ev, data)
{
	PROCESS_BEGIN();
	rest_init_engine();
	rest_activate_resource(&setting, "setting");
	rest_activate_resource(&light, "light");
	while(1) {
   		 PROCESS_WAIT_EVENT();
	}
	PROCESS_END();
}