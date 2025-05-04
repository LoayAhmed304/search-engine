<template>
  <footer>
    <p>Created by a group of passionate CMP27 students © 2025 All rights reserved</p>
  </footer>
</template>

<style scoped>
footer {
  position: fixed;
  bottom: 0;
  width: 90%;
  padding: 25px 15px 45px;
  display: flex;
  justify-content: center;
}
footer p {
  font-size: 0.85rem;
  color: var(--text-secondary-color, #666);
  opacity: 0.8;
  font-weight: 400;
  letter-spacing: 0.5px;
  margin: 0;
  padding: 0;
  transition: all 0.2s ease;
}

footer p:hover {
  opacity: 1;
  color: var(--accent-color, #4285f4);
}
</style>