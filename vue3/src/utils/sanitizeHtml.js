import DOMPurify from 'dompurify'

const DEFAULT_ALLOWED_TAGS = [
  'br',
  'p',
  'strong',
  'em',
  'ul',
  'ol',
  'li',
  'blockquote',
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6'
]

const DEFAULT_ALLOWED_ATTR = ['class']

export function sanitizeHtml(input, options = {}) {
  return DOMPurify.sanitize(String(input || ''), {
    ALLOWED_TAGS: options.allowedTags || DEFAULT_ALLOWED_TAGS,
    ALLOWED_ATTR: options.allowedAttr || DEFAULT_ALLOWED_ATTR
  })
}

export function sanitizeTextWithBreaks(input) {
  const withBreaks = String(input || '').replace(/\n/g, '<br>')
  return sanitizeHtml(withBreaks, {
    allowedTags: ['br', 'strong', 'em']
  })
}
