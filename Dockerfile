# Use the official nginx alpine image for small size
FROM nginx:alpine

# Remove default nginx content (optional)
RUN rm -rf /usr/share/nginx/html/*

# Copy site files into nginx's html folder
COPY site /usr/share/nginx/html



# Expose port 80
EXPOSE 80


