# Sample Node.js app
FROM node:18

WORKDIR /app
COPY . .
RUN npm install

CMD ["npm", "start"]
