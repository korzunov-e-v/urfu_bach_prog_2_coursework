build:
	docker compose build

up:
	docker compose up -d --force-recreate

down:
	docker compose down

up-app:
	docker compose up cw2-bot -d --force-recreate

down-app:
	docker compose down cw2-bot
