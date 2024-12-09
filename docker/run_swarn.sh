docker network create --driver overlay virtualboss
docker service create --network virtualboss --replicas 1 --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=virtualboss -dp 5432:5432 postgres
docker service create --network virtualboss --replicas 1 --name virtualboss -e DATABASE_URL=jdbc:postgresql://192.168.0.15:5432/virtualboss -dp 8008:8080 ragaev/virtualboss:0.0.1-SNAPSHOT
docker service create --replicas 3 --name frontend -dp 80:80 ragaev/frontend:virtualboss -v ./docker/nginx-config:/etc/nginx/conf.d

node1_id=$(docker node list | grep worker1 | awk '{print $1}')
docker node update --label-add type=postgres ${node1_id?}

docker stack deploy --compose-file=./docker-compose.yml vb-stack