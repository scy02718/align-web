# ----------------------------
# 1. Build stage
# ----------------------------
FROM node:22-alpine AS builder
WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm install

# Copy app source code
COPY . .

# Build Next.js application
RUN npm run build

# Remove dev dependencies to reduce image size
RUN npm prune --production

# ----------------------------
# 2. Production stage
# ----------------------------
FROM node:22-alpine
WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app ./

# Expose the port (must match the one in Next.js `package.json`)
EXPOSE 3000

# Run the Next.js application
CMD ["npm", "start"]