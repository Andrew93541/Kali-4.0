const router = require('express').Router();
const multer = require('multer');
const path = require('path');
const { authenticateToken } = require('../middleware/auth');
const { uploadMedia, getMedia } = require('../controllers/mediaController');

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, path.join(__dirname, '../../uploads')),
  filename: (req, file, cb) => cb(null, `${Date.now()}_${file.originalname}`),
});
const upload = multer({ storage });

router.use(authenticateToken);
router.post('/', upload.single('file'), uploadMedia);
router.get('/:alert_id', getMedia);

module.exports = router;
