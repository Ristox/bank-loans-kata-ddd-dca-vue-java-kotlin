FROM node:lts AS build

WORKDIR /opt/rsx/bank-loans-ui
COPY . . 

ARG VITE_BASE_URL
ENV VITE_BASE_URL=$VITE_BASE_URL

RUN rm -rf node_modules/ package-lock.json && npm install && npm run build



FROM nginx:alpine AS deploy

ARG VITE_BASE_URL
ENV VITE_BASE_URL=$VITE_BASE_URL

COPY --from=build /opt/rsx/bank-loans-ui/dist  /var/www/rsx-bank-loans-ui

RUN chown nginx:nginx /var/www && rm /etc/nginx/conf.d/default.conf
COPY .nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.template

CMD ["/bin/sh" , "-c" , "envsubst '$$VITE_BASE_URL $$WEB_PORT' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
