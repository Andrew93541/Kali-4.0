const errorHandler = (err, req, res, next) => {
  console.error('❌ [Server Error Log]:', err.stack || err.message || err);

  const status = err.status || 500;
  const isProd = process.env.NODE_ENV === 'production';

  res.status(status).json({
    error: {
      message: isProd && status === 500 ? 'Internal Server Error' : err.message || 'Something went wrong',
      ...(isProd ? {} : { stack: err.stack, details: err.details })
    }
  });
};

module.exports = errorHandler;
