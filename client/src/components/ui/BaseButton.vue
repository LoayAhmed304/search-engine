<script setup>
const emit = defineEmits(['button-click']);

</script>

<template>
  <button class="base-button" @click="$emit('button-click')">
    <slot />
  </button>
</template>

<style scoped>
button {
  background-color: var(--accent-color);
  border: none;
  padding: 10px 16px;
  font-size: 1.2rem;
  margin: 1rem;
  cursor: pointer;
  border-radius: 5px;
  color: var(--background-color);
  opacity: 0.85;
}
button:hover {
  opacity: 1;
}
</style>