<template>
  <button class="base-button" @click="$emit('button-click')">
    <slot />
  </button>
</template>

<script setup>
const emit = defineEmits(['button-click']);

</script>

<style scoped>
.base-button {
  background-color: var(--accent-color);
  border: none;
  padding: 10px 16px;
  font-size: 1.2rem;
  /* margin: 1rem ; */
  cursor: pointer;
  border-radius: 5px;
  color: var(--background-color);
  opacity: 0.85;
}
.base-button:hover {
  opacity: 1;
}
</style>