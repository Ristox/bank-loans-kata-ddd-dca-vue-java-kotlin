FROM node:lts AS build

WORKDIR /opt/rsx/bank-loans-ui
COPY . . 
RUN rm -rf node_modules/
RUN rm package-lock.json
RUN npm install
RUN npm run build
RUN ls -la dist/*

FROM nginx:alpine AS deploy

COPY --from=build /opt/rsx/bank-loans-ui/dist  /var/www/rsx-bank-loans-ui
RUN ls -la /var/www/rsx-bank-loans-ui/
RUN rm /etc/nginx/conf.d/default.conf
COPY .nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf
