<template>
  <div class="spinner-container">
    <div class="spinner" :style="spinnerStyle">
      <div class="spinner__circle"></div>
    </div>
    <p v-if="text" class="spinner__text">{{ text }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  size: {
    type: String,
    default: 'medium' 
  },
  text: {
    type: String,
    default: 'Loading...'
  },
  color: {
    type: String,
    default: '' 
  }
});

const spinnerStyle = computed(() => {
  const styles = {};
  
  if (props.color) {
    styles['--spinner-color'] = props.color;
  }
  
  return styles;
});
</script>

<style scoped>
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.spinner {
  position: relative;
  width: 50px;
  height: 50px;
}

.spinner__circle {
  box-sizing: border-box;
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-top-color: var(--spinner-color, var(--accent-color, #4285f4));
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner__text {
  margin-top: 1rem;
  color: var(--text-color, #444);
  font-size: 0.9rem;
}

.spinner-container[size="small"] .spinner {
  width: 30px;
  height: 30px;
}

.spinner-container[size="large"] .spinner {
  width: 70px;
  height: 70px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>