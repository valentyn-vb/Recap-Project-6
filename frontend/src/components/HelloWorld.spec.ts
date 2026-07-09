import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import HelloWorld from './HelloWorld.vue'

describe('HelloWorld', () => {
  it('increments the counter when the button is clicked', async () => {
    const wrapper = mount(HelloWorld)
    const button = wrapper.get('button.counter')

    expect(button.text()).toContain('Count is 0')

    await button.trigger('click')

    expect(button.text()).toContain('Count is 1')
  })
})
