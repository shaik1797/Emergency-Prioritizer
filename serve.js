const http = require('http');
const fs = require('fs');
const path = require('path');

const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  '.pdf': 'application/pdf'
};

const server = http.createServer((req, res) => {
  let safeUrl = req.url.split('?')[0];
  let filePath = path.join(PUBLIC_DIR, safeUrl === '/' ? 'index.html' : safeUrl);

  if (!fs.existsSync(filePath) && safeUrl === '/Emergency_Prioritizer_Presentation.pptx') {
    filePath = path.join(__dirname, 'Emergency_Prioritizer_Presentation.pptx');
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('File Not Found');
      } else {
        res.writeHead(500);
        res.end(`Server Error: ${err.code}`);
      }
    } else {
      const headers = {
        'Content-Type': contentType,
        'Access-Control-Allow-Origin': '*'
      };
      if (ext === '.pptx') {
        headers['Content-Disposition'] = 'attachment; filename="Emergency_Prioritizer_Presentation.pptx"';
      }
      res.writeHead(200, headers);
      res.end(content);
    }
  });
});

server.listen(3000, () => {
  console.log('Static presentation server running on port 3000');
});
