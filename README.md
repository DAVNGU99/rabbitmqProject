# rabbitmqProject

Learning rabbitMQ rawdogging the docs

docker run -d \
  --hostname my-rabbit \
  --name rabbitmq \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:management
